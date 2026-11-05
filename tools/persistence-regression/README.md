# Persistence regression probes

These integration probes exercise actual ValhallaMMO persistence methods and SQLite writes inside Paper. They compare the unmodified 1.10.3 behavior with this change. They are deliberately separate from Maven's unit-test lifecycle because they require a prepared Paper runtime and two plugin JARs.

Use a stopped, initialized Paper runtime as the template. It must contain `paper.jar`, `libraries/`, `versions/`, `cache/`, an accepted `eula.txt`, and `plugins/ValhallaMMO/` with its configuration and SQLite schema. Use a JDK compatible with that Paper release. The probes were developed with Paper 26.2-134 and Java 25; their Java sources target release 17.

```sh
python tools/persistence-regression/run.py \
  --runtime /path/to/stopped-paper-runtime \
  --baseline-jar /path/to/original-ValhallaMMO-1.10.3.jar \
  --candidate-jar /path/to/patched-ValhallaMMO.jar \
  --jdk /path/to/jdk-25 \
  --output /path/to/new-test-directory
```

The template is read-only. Each test has a separate server directory and SQLite database; player rows are deleted from those test copies before startup. Do not use an active server as the template. The new test servers bind only to `127.0.0.1` on an automatically allocated port. Logs and the final `results.json` remain in the output directory.

## Covered cases

- Block all persistence executor workers, enqueue an asynchronous save, and attempt a synchronous save. The original skips the synchronous write; the candidate writes the current profile. Releasing the queue may eventually let the original async write succeed, so this case establishes a skipped synchronous flush, not guaranteed data loss on every normal shutdown.
- Inject a runtime exception during a synchronous write, then retry. The original leaves its saving marker stuck; the candidate can save again.
- Save an offline player's ordinary and configurable profiles. The original can evict the configurable family before writing it; the candidate preserves both.
- Reject a write with a SQLite trigger. The original swallows the error and evicts the fresh cached profile; the candidate reports failure and retains it for retry.
- Temporarily hide a table in the isolated database. The original treats the read failure like a missing player row; the candidate fails loading instead.
- Check dirty level-zero and ownerless profiles.
- Delay a manual save and record whether the completion message precedes the actual SQL write.
- Fail one periodic save, then wait for another interval. The original scheduled task stops executing; the candidate retries.
- Leave an ordinary Power profile, perks, and points cached for the plugin's real shutdown hook, restart the process, and verify the stored values.

The baseline assertions intentionally expect the old defects; a passing baseline run means they were reproduced. The candidate assertions expect the corrected behavior. Expected: 6 baseline race checks, 11 baseline fault checks, 8 candidate race checks, 11 candidate fault checks, and 3 candidate restart checks.

These are controlled fault-injection tests, not a real-client gameplay test. They do not establish the root cause of any particular production rollback. MySQL and Redis multi-server behavior are not validated by this harness. `RedisLockedSQL` overrides the ordinary save method and its distributed locking/queue lifecycle is outside this patch's scope. Saving is not made transactional across an entire account, and mutable in-memory profile statistics are not made into atomic snapshots by this change.
