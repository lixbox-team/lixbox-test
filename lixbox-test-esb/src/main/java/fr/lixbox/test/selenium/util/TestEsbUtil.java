/*******************************************************************************
 *    
 *                           FRAMEWORK Lixbox
 *                          ==================
 *      
 * This file is part of lixbox-test.
 *
 *    lixbox-supervision is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    lixbox-supervision is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *    along with lixbox-test.  If not, see <https://www.gnu.org/licenses/>
 *   
 *   @AUTHOR Lixbox-team
 *
 ******************************************************************************/
package fr.lixbox.test.selenium.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Assertions;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Cette classe regroupe des facilites pour évaluer les routes ESB.
 * 
 * @author ludovic.terral
 */
public class TestEsbUtil
{
    // ----------- Attribut -----------
    private static final Log LOG = LogFactory.getLog(TestEsbUtil.class);



    // ----------- Methode -----------
    private TestEsbUtil()
    {
        LOG.trace("Initialisation du TestEsbUtil");
    }



    public static void launchLocalTransport(File from, File to)
            throws IOException, InterruptedException
    {
        FileUtils.moveFile(from, to);
        int i=1;
        do
        {
            TimeUnit.SECONDS.sleep(5);
            i++;
        }
        while(i<10 && Files.exists(to.toPath()));
        TimeUnit.SECONDS.sleep(10);
    }  



    public static void launchRemoteTransport(Session session, File from, String to) throws Exception
    {
        try (InputStream inputStream = new FileInputStream(from))
        {
            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            sftpChannel.cd(to.substring(0, to.lastIndexOf("/")));
            OutputStream outputStream = sftpChannel.put(to);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) 
            {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            outputStream.close();
            sftpChannel.disconnect();
            session.disconnect();
        } 
        catch (JSchException | java.io.IOException e) 
        {
            LOG.fatal(e);
            Assertions.fail(e);
        }
        
      int i=1;
      do
      {
          TimeUnit.SECONDS.sleep(5);
          i++;
      }
      while(i<10 && remoteFileExist(session, to));
      TimeUnit.SECONDS.sleep(10);
    }



    public static boolean remoteFileExist(Session session, String destination)
    {
        boolean result = false;
        try 
        {
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand("cat " + destination);
            channel.setInputStream(null);
            InputStream in=channel.getInputStream();
            channel.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder fileContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) 
            {
                fileContent.append(line);
            }
            channel.disconnect();
            session.disconnect();
            result = fileContent.length()>0;
        } 
        catch (Exception e) 
        {
            LOG.fatal(e);
            result = false;
        }
        return result;
    }
    
    
    
    public static void verifyLocalFile(File destination, Integer size, boolean checkDatas) throws Exception
    {
        verifyLocalFile(destination, new byte[0], size, false);
    }
    
    
    
    public static void verifyLocalFile(File destination, byte[] datas, Integer size, boolean checkDatas) throws Exception
    {
        Assertions.assertTrue(Files.exists(destination.toPath()), "le fichier "+destination+" est absent");
        Assertions.assertEquals(FileUtils.readFileToByteArray(destination).length, size, "la taille du fichier est incorrecte");
        if (checkDatas)
        {
            Assertions.assertTrue(Arrays.equals(FileUtils.readFileToByteArray(destination), datas), "le contenu du fichier n'est pas identique");
        }
    }



    public static void verifyRemoteFile(Session session, String destination, byte[] datas, Integer size, boolean checkDatas) throws Exception
    {
        try 
        {
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand("cat " + destination);
            channel.setInputStream(null);
            InputStream in=channel.getInputStream();
            channel.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder fileContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) 
            {
                fileContent.append(line);
            }
            channel.disconnect();
            session.disconnect();
            
            Assertions.assertTrue(fileContent.length()>0, "le fichier "+destination+" est absent");
            Assertions.assertEquals(fileContent.length(), size, "la taille du fichier "+destination+" est incorrecte");
            if (checkDatas)
            {
                Assertions.assertTrue(Arrays.equals(fileContent.toString().getBytes(), datas), "le contenu du fichier "+destination+" est différent de celui attendu");
            }
        } 
        catch (JSchException | java.io.IOException e) 
        {
            LOG.fatal(e);
            Assertions.fail(e);
        }
    }
    
    
    
    public static void mkRemoteDirBySsh(Session session, String directoryPath)
    {
        try 
        {
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand("mkdir -p " + directoryPath);
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);
            channel.connect();
            channel.disconnect();
            session.disconnect();
            LOG.info("Directory "+directoryPath+" created successfully.");
        } 
        catch (JSchException e) 
        {
            LOG.fatal(e);
            Assertions.fail(e); 
        }
    }
    
    
    
    public static void removeRemoteDirBySsh(Session session, String directoryPath)
    {
        try 
        {
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand("rm -rf " + directoryPath);
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);
            channel.connect();
            channel.disconnect();
            session.disconnect();
            LOG.info("Directory "+directoryPath+" created successfully.");
        } 
        catch (JSchException e) 
        {
            LOG.fatal(e);
            Assertions.fail(e); 
        }
    }
}