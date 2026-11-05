"""Compare persistence behavior in two ValhallaMMO JARs on isolated Paper servers.

Requires an existing, stopped Paper runtime with libraries/, versions/, cache/,
plugins/ValhallaMMO/config.yml and an initialized SQLite schema. The runtime and
its original player data are only read. Every server uses a separate database
whose player rows have been removed before startup.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import sqlite3
import subprocess
import tempfile
import zipfile


def run_checked(command, **kwargs):
    subprocess.run(list(map(str, command)), check=True, **kwargs)


def prepare(runtime, output, label, jar, plugin, baseline, faults):
    server = output / label
    server.mkdir()
    for name in ("libraries", "versions", "cache"):
        shutil.copytree(runtime / name, server / name)
    for name in ("paper.jar", "eula.txt", "bukkit.yml", "spigot.yml"):
        if (runtime / name).is_file():
            shutil.copy2(runtime / name, server / name)
    data = server / "plugins/ValhallaMMO"
    shutil.copytree(runtime / "plugins/ValhallaMMO", data,
                    ignore=shutil.ignore_patterns("*.db*", "*.jar", "*.log*", "parties.json"))
    source = runtime / "plugins/ValhallaMMO/player_data.db"
    with sqlite3.connect(source.as_uri() + "?mode=ro", uri=True) as existing:
        with sqlite3.connect(data / "player_data.db") as target:
            existing.backup(target)
            for (table,) in target.execute("SELECT name FROM sqlite_master WHERE type='table'").fetchall():
                target.execute('DELETE FROM "' + table.replace('"', '""') + '"')
            target.commit()
            target.execute("VACUUM")
    config = data / "config.yml"
    text = config.read_text(encoding="utf-8-sig")
    # Accelerate only the periodic-task test, not the production default.
    delay = 1000 if faults else 300000
    text = re.sub(r"^db_persist_delay:.*$", f"db_persist_delay: {delay}", text, flags=re.M)
    text = re.sub(r"^check_updates:.*$", "check_updates: false", text, flags=re.M)
    config.write_text(text, encoding="utf-8")
    shutil.copy2(jar, server / "plugins/ValhallaMMO.jar")
    shutil.copy2(plugin, server / "plugins/SaveRaceProbe.jar")
    if baseline:
        (server / "baseline").touch()
    (server / "server.properties").write_text(
        "server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\n"
        "view-distance=2\nsimulation-distance=2\nlevel-name=persistence-test\n"
        "level-type=minecraft:flat\ngenerate-structures=false\nmax-tick-time=120000\n",
        encoding="utf-8")
    return server


def launch(java, server, label):
    passed = server / "race-passed.txt"
    failed = server / "race-failed.txt"
    # Only these probe-generated result markers are replaced on a restart.
    for marker in (passed, failed):
        if marker.exists():
            marker.unlink()
    with (server / f"{label}.log").open("w", encoding="utf-8") as log:
        run_checked([java, "-Dterminal.jline=false", "-Dterminal.ansi=false",
                     "-Xms256M", "-Xmx1200M", "-Xverify:all", "-jar", "paper.jar", "--nogui"],
                    cwd=server, stdin=subprocess.DEVNULL, stdout=log,
                    stderr=subprocess.STDOUT, timeout=240,
                    **({"creationflags": subprocess.CREATE_NO_WINDOW} if os.name == "nt" else {}))
    if not passed.is_file():
        raise RuntimeError(f"{label} failed; inspect {server / (label + '.log')}")
    log = (server / f"{label}.log").read_text(encoding="utf-8", errors="replace")
    checks = [line for line in log.splitlines() if "[SaveRaceProbe] PASS " in line]
    print(f"{label}: {len(checks)} checks passed", flush=True)
    return {"checks": len(checks), "log": str(server / f"{label}.log")}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--runtime", type=Path, required=True)
    parser.add_argument("--baseline-jar", type=Path, required=True)
    parser.add_argument("--candidate-jar", type=Path, required=True)
    parser.add_argument("--jdk", type=Path, required=True, help="JDK home, containing bin/java and bin/javac")
    parser.add_argument("--output", type=Path, help="New directory; an existing directory is never overwritten")
    args = parser.parse_args()
    runtime = args.runtime.resolve()
    for file in (args.baseline_jar, args.candidate_jar, runtime / "plugins/ValhallaMMO/player_data.db"):
        if not file.is_file():
            parser.error(f"Missing required file: {file}")
    if args.output:
        output = args.output.resolve()
        output.mkdir(parents=True, exist_ok=False)
    else:
        output = Path(tempfile.mkdtemp(prefix="valhallammo-persistence-"))
    suffix = ".exe" if os.name == "nt" else ""
    java, javac = (args.jdk / "bin" / (name + suffix) for name in ("java", "javac"))
    classes = output / "classes"
    classes.mkdir()
    sources = Path(__file__).resolve().parent
    classpath = os.pathsep.join(map(str, [args.baseline_jar.resolve(), *runtime.glob("libraries/**/*.jar")]))
    run_checked([javac, "--release", "17", "-encoding", "UTF-8", "-cp", classpath,
                 "-d", classes, sources / "SaveRaceProbe.java", sources / "SaveFaultProbe.java"])
    plugins = {}
    for main_class in ("SaveRaceProbe", "SaveFaultProbe"):
        plugin = output / f"{main_class}.jar"
        with zipfile.ZipFile(plugin, "w", zipfile.ZIP_DEFLATED) as archive:
            for file in classes.glob("*.class"):
                archive.write(file, file.name)
            archive.writestr("plugin.yml", f"name: SaveRaceProbe\nversion: 1\nmain: {main_class}\n"
                              "api-version: '1.21'\ndepend: [ValhallaMMO]\n")
        plugins[main_class] = plugin
    results = {}
    for kind, jar, baseline in (("baseline", args.baseline_jar, True), ("candidate", args.candidate_jar, False)):
        for faults in (False, True):
            label = f"{kind}-{'faults' if faults else 'races'}"
            server = prepare(runtime, output, label, jar.resolve(),
                             plugins["SaveFaultProbe" if faults else "SaveRaceProbe"], baseline, faults)
            results[label] = launch(java, server, label)
            if not baseline and not faults:
                (server / "restart-verify").touch()
                results["candidate-restart"] = launch(java, server, "candidate-restart")
    report = {"baseline_sha256": hashlib.sha256(args.baseline_jar.read_bytes()).hexdigest(),
              "candidate_sha256": hashlib.sha256(args.candidate_jar.read_bytes()).hexdigest(),
              "results": results}
    (output / "results.json").write_text(json.dumps(report, indent=2), encoding="utf-8")
    print(f"Full report: {output / 'results.json'}")


if __name__ == "__main__":
    main()
