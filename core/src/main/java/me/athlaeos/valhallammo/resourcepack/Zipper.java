package me.athlaeos.valhallammo.resourcepack;

import me.athlaeos.valhallammo.ValhallaMMO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Zipper {
    public static void zipFolder(String sourceDirPath, String zipFilePath) {
        try {
            FileOutputStream fileStream = new FileOutputStream(zipFilePath);
            ZipOutputStream zipFile = new ZipOutputStream(fileStream);
            File zippingFile = new File(sourceDirPath);
            zipFile(zippingFile, zippingFile.getName(), zipFile);
            zipFile.close();
            fileStream.close();
        } catch (IOException ex) {
            ValhallaMMO.logWarning("Could not zip resource pack");
            ex.printStackTrace();
        }
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipFileOutputStream) throws IOException {
        byte[] bytes = new byte[1024];
        int length;
        if (fileToZip.isDirectory()) {
            File[] children = fileToZip.listFiles();

            assert children != null;

            length = children.length;

            for(int var7 = 0; var7 < length; ++var7) {
                File childFile = children[var7];
                zipSubFile(childFile, childFile.getName(), zipFileOutputStream);
            }
        } else {
            try (FileInputStream fis = new FileInputStream(fileToZip)){
                ZipEntry zipEntry = new ZipEntry(fileName);
                zipFileOutputStream.putNextEntry(zipEntry);

                while((length = fis.read(bytes)) > 0) {
                    zipFileOutputStream.write(bytes, 0, length);
                }
            }
        }
    }

    private static void zipDirectory(File fileToZip, String fileName, ZipOutputStream zipFileOutputStream) throws IOException {
        if (!fileToZip.isDirectory()) return;
        File[] children = fileToZip.listFiles();
        if (children == null) return;
        for (File childFile : children) {
            zipSubFile(childFile, fileName + "/" + childFile.getName(), zipFileOutputStream);
        }
    }

    private static void zipSubFile(File fileToZip, String fileName, ZipOutputStream zipFileOutputStream) throws IOException {
        if (fileToZip.isDirectory()) {
            zipDirectory(fileToZip, fileName, zipFileOutputStream);
        } else {
            byte[] bytes = new byte[1024];
            try (FileInputStream fis = new FileInputStream(fileToZip)){
                ZipEntry zipEntry = new ZipEntry(fileName);
                zipFileOutputStream.putNextEntry(zipEntry);

                int length;
                while((length = fis.read(bytes)) > 0) {
                    zipFileOutputStream.write(bytes, 0, length);
                }
            }
        }
    }

    public static void unzipFolder(String zipFile, File destination){
        try (ZipInputStream in = new ZipInputStream(new FileInputStream(zipFile))){
            byte[] buffer = new byte[1024];
            ZipEntry entry = in.getNextEntry();
            while (entry != null){
                File newFile = newFile(destination, entry);
                if (entry.isDirectory()){
                    if (!newFile.isDirectory() && !newFile.mkdirs()) throw new IOException("Could not make directory for zipped file " + entry.getName());
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) throw new IOException("Could not make directory for zipped file " + entry.getName());

                    FileOutputStream out = new FileOutputStream(newFile);
                    int read;
                    while ((read = in.read(buffer)) > 0){
                        out.write(buffer, 0, read);
                    }
                    out.close();
                }
                entry = in.getNextEntry();
            }
        } catch (IOException ex){
            ValhallaMMO.logWarning("Could not unzip " + zipFile);
            ex.printStackTrace();
        }
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
