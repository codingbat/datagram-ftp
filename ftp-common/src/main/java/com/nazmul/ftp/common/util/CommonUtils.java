package com.nazmul.ftp.common.util;

import com.nazmul.ftp.common.exception.InvalidArgException;
import com.nazmul.ftp.common.io.FileEvent;
import com.nazmul.ftp.common.protocol.ResponseCode;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class CommonUtils {

  private CommonUtils() {

  }

  public static short extractOpcode(String str) throws InvalidArgException {

    if (str == null || str.isEmpty()) {
      throw new InvalidArgException("Invalid string provided");
    }

    int opIndex = str.indexOf('!');
    return Short.parseShort(str.substring(0, opIndex));
  }

  public static String extractUsername(String str) {

    int opIndex = str.indexOf('!') + 1;
    int passIndex = str.indexOf('@');
    return str.substring(opIndex, passIndex);
  }

  public static String extractPassword(String str) {

    int passIndex = str.indexOf('@') + 1;
    int lastIndex = str.lastIndexOf('!');
    return str.substring(passIndex, lastIndex);
  }

  public static boolean fieldStartsWith(String str, char ch) {

    return str.charAt(0) == ch;
  }

  public static boolean fieldEndsWith(String str, char ch) {

    return str.charAt(str.length() - 1) == ch;
  }

  public static void createAndWriteFile(FileEvent fileEvent, String username)
          throws IOException, InvalidArgException {

    String destinationPath = fileEvent.getDestinationDirectory() + File.separator + username;
    String outputFile = destinationPath + File.separator + fileEvent.getFilename();
    if (!new File(destinationPath).exists()) {
      new File(destinationPath).mkdirs();
    }
    try {
      File dstFile = new File(outputFile);
      OutputStream fileOutputStream = new FileOutputStream(dstFile);
      fileOutputStream.write(fileEvent.getFileData());
      fileOutputStream.flush();
      fileOutputStream.close();
    } catch (FileNotFoundException e) {
      throw new InvalidArgException(String.valueOf(ResponseCode.REQUESTED_ACTION_NOT_TAKEN));
    }
  }

  public static FileEvent getFileEvent(String sourceFilePath, String destinationPath) {

    FileEvent fileEvent = new FileEvent();
    String path = sourceFilePath.substring(0, sourceFilePath.lastIndexOf(File.separator) + 1);
    fileEvent.setDestinationDirectory(destinationPath);
    String fileName = sourceFilePath.substring(sourceFilePath.lastIndexOf(File.separator) + 1, sourceFilePath.length());
    fileEvent.setFilename(fileName);
    fileEvent.setSourceDirectory(sourceFilePath);
    File file = new File(sourceFilePath);
    if (file.isFile()) {
      try {
        DataInputStream diStream = new DataInputStream(new FileInputStream(file));
        Long len = file.length();
        byte[] fileBytes = new byte[len.intValue()];
        int read = 0;
        int numRead = 0;
        while (read < fileBytes.length && numRead >= 0) {
          read += numRead;
          numRead = diStream.read(fileBytes, read, fileBytes.length - read);
        }
        fileEvent.setFileSize(len);
        fileEvent.setFileData(fileBytes);
        fileEvent.setStatus("Success");
      } catch (Exception e) {
        fileEvent.setStatus("Error");
      }
    } else {
      fileEvent.setStatus("Error");
    }
    return fileEvent;
  }
}
