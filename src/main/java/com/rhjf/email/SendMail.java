package com.rhjf.email;

import java.io.UnsupportedEncodingException;  
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.codec.binary.Base64;




public class SendMail {
	public boolean sendMail(MailBean mb) {
		String host = mb.getHost();
		final String username = mb.getUsername();
		final String password = mb.getPassword();
		String from = mb.getFrom();
		String subject = mb.getSubject();
		String content = mb.getContent();
		String fileName = mb.getFilename();
		Vector<String> file = mb.getFile();

		Properties props = System.getProperties();
		// mail.smtp.host
		props.setProperty("mail.smtp.host", host); // 设置SMTP的主机
		props.setProperty("mail.smtp.auth", "true"); // 需要经过验证

		props.setProperty("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.port", "25");
		props.setProperty("mail.smtp.socketFactory.port", "25");
          //Session s = Session.getInstance(props, null);
		
		Session session = Session.getInstance(props, new Authenticator() {
			@Override
			public PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {
			MimeMessage msg = new MimeMessage(session);
			// 设置多个收件人地址  
            List<String> list = mb.getTo();  
            
            String toAddress = getAddress(list);  
			
			msg.setFrom(new InternetAddress(from));
			
			InternetAddress[] address = InternetAddress.parse(toAddress);  
			msg.setRecipients(Message.RecipientType.TO, address);
			
			msg.setSubject(subject);

			// 设置抄送人
			if (mb.getCopyColumn() != null) {
				msg.setRecipients(Message.RecipientType.CC,
						(Address[]) InternetAddress.parse(getAddress(mb.getCopyColumn())));
			}

			Multipart mp = new MimeMultipart();
			MimeBodyPart mbpContent = new MimeBodyPart();
			// 发送html格式邮件
			mbpContent.setContent(content , "text/html; charset=utf-8");
			mp.addBodyPart(mbpContent);

			/* 往邮件中添加附件 */
			if (file != null) {
				Enumeration<String> efile = file.elements();
				while (efile.hasMoreElements()) {
					MimeBodyPart mbpFile = new MimeBodyPart();
					fileName = efile.nextElement().toString();
					FileDataSource fds = new FileDataSource(fileName);
					mbpFile.setDataHandler(new DataHandler(fds));
					try {
						mbpFile.setFileName("=?GBK?B?"
								+ Base64.encodeBase64String(fds.getName().getBytes("GBK"))
								+ "?=");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					mp.addBodyPart(mbpFile);
				}
			}

			msg.setContent(mp);
			msg.setSentDate(new Date());
			Transport.send(msg);

		} catch (MessagingException me) {
			me.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	
	public static void sendMail(String title , String context , String[] to , String[] CopyColumn  , String[] files){
		
		
		MailBean mb = new MailBean();
		mb.setHost("smtp.qiye.163.com"); // 设置SMTP主机(163)，若用126，则设为：smtp.126.com
		mb.setUsername("zhoufangyu@ronghuijinfubj.com"); // 设置发件人邮箱的用户名
		mb.setPassword("siyanlv3@"); // 设置发件人邮箱的密码，需将*号改成正确的密码
		mb.setFrom("zhoufangyu@ronghuijinfubj.com"); // 设置发件人的邮箱
		
		
		mb.setSubject(title); // 设置邮件的主题
		mb.setContent(context); // 设置邮件的正文
		
		// 设置收件人的邮箱
		for (String string : to) {
			mb.setTo(string);
		}
		
		if(CopyColumn!=null){
			//  设置抄送人
			for (String copy :CopyColumn) {
				mb.setCopyColumn(copy);
			}
		}
		
		//  添加附件
		if(files!=null){
			for (String file : files) {
				mb.attachFile(file); 
			}
		}
		SendMail sm = new SendMail();
		sm.sendMail(mb); // 发送邮件
	}

	public String getAddress(List<String> list){
		String address = "";
		for (String string : list) {
			address += string + ",";
		}
		return address;
	}
}
