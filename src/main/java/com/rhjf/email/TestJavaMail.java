package com.rhjf.email;

public class TestJavaMail {
	public static void main(String[] args) {

//		String[] youjian = {"128995@qq.com","1234652@163.com","309535058@qq.com","841484531@qq.com","18031572928@qq.com","18031311779@qq.com",
//				"13739893616@qq.com","463519042@qq.com","3350289829@qq.com","731629828@qq.com","15004284284@qq.com","15930555480@qq.com","402310369@qq.com",
//				"2780694648@qq.com","13315547961@qq.com","111@163.com","13832929505@qq.com","15641685011@qq.com","64@163.com","15542915488@163.com",
//				"15102509369@qq.com","15512528128@qq.com","13130959628@qq.com","13363391399@163.com","15142998096@qq.com","13402401026@163.com",
//				"15524042658@qq.com","15041665024@163.com","1063896519@qq,com","13191789348@qq.com","15042974185@qq.com","2880343@qq.com",
//				"tyt0909@163.com","13942970391@qq.com","17731580858@qq.com","15631553555@163.com","407117583@qq.com","18532573134@qq.com",
//				"118066228@qq.com","343111035@qq.com","miao125874@foxmail.com","49682694@qq.com","13464689565","13897826300@163.com",
//				"17717757001@qq.com","17713172308@qq.com","18134056816@qq.com","zhuyulin2005@yahoo.com","1187894498@qq.com",
//				"16312306@qq.com","646378479@qq.com","15633900888@163.com","liukeda@ronghuijinfubj.com","13784067873@qq.com",
//				"18642964375@qq.com","13323055890@qq.com","15303250129@qq.com","15754182251@163.com","234439938@qq.com",
//				"15133951580@qq.com","15141602096@qq.com","18730586968@qq.com","1193239483","18831568066@qq.com","18630518586",
//				"siton@vip.qq.com","13673294770@qq.com","873524779@qq.com","476520335@qq.com","23424@163.com","1530045@qq.com",
//				"liukeda@ronghuijinfubj.com","15040957512@qq.com","13465@163.com","15932088613@qq.com","2254497009@qq.com",
//				"13898935200@qq.com","15898284569@qq.com","419885575@qq.com","13930545297@qq.com","15932588251@qq.com","1465@163.com",
//				"269388570@qq.com","kunxiangkeji163.com","1102297504@qq.com","273018910@qq.com","18731523406@qq.com","15932585063@qq.com",
//				"13784666008@qq.com","15931573678@qq.com","15084145595@qq.com","15831505546","18730519186@qq.com","157172636@qq.com",
//				"15033513016@qq.com","15100509096","18242962408@qq.com","15373158615@163.com","tulei521ly@sina.com","13932542547@163.com",
//				"guoqianghzzl@163.com","15232451596@qq.com","1927900586@qq.com","1530045@qq.com","13231569999@qq.com","18732513816@qq.com",
//				"15142893598@qq.com","254080589@qq.com","172126762@qq.com","15040981530@qq.com","15832523667@qq.com","15042672357@163.com",
//				"18330514685@163.com","17732513007","13763837370@139.com","15933403888@qq.com","15930898161@qq.com","731629828@qq.com",
//				"13591859607@163.com","15318915@qq.com","15704233369","17330581858@qq.com","15898253556@163.com","18642931854@qq.com",
//				"13931410109@qq.com","18732512619@qq.com","278193255@qq.com","13910405304@qq.com","2423423@163.com",
//				"13840668442@163.com","15632820268@163.com","21312@qq.com","15097579085@qq.com","13333159577@qq.com","15898226011@qq.com"};
		
		String[] youjian = {"pengyangyang@ronghuijinfubj.com"};
		
		
		MailBean mb = new MailBean();
		mb.setHost("smtp.qiye.163.com"); // 设置SMTP主机(163)，若用126，则设为：smtp.126.com
		mb.setUsername("support@ronghuijinfubj.com"); // 设置发件人邮箱的用户名
		mb.setPassword("qiye@163"); // 设置发件人邮箱的密码，需将*号改成正确的密码
		mb.setFrom("support@ronghuijinfubj.com"); // 设置发件人的邮箱
		
		
		for (int i = 0; i < youjian.length; i++) {
			
			String youxiang = youjian[i];
			
			mb.setTo(youxiang);// 设置收件人的邮箱
			
			
			mb.setSubject("【爱码付团队】 爱码付业务员app上线通知"); // 设置邮件的主题
			mb.setContent("<h3>亲爱的代理商：</h3><p>您好！感谢您使用爱码付，见证着爱码付成长。</p><p>爱码付业务员版APP上线了。</p><p><h6>功能介绍：</h6></p><p>1.通过爱码付代理商后台（www.shanglianchu.cn:8182/business/）创建销售人员，通过创建的账号及密码登录爱码付业务员APP；</p><p>2.爱码付业务员APP可添加商户（便于代理商更高效的拓展市场），享受市场收益及代理商让利；</p><p>3.爱码付业务员APP实现了变更商户会员等级。</p><p><h6>下载需知：</h6></p><p>IOS：苹果商店（apple store）搜索“爱码付业务员”进行下载；</p><p>安卓：通过“百度手机助手”“360手机助手”“91助手”“安卓市场”进行下载安装。</p><p><h6>特此声明：</h6></p><p>此前创建代理商时自动生成“爱码付分享用户”将作为真实用户处理，"
					+ "在后续的运营阶段不再为代理商自动生成该用户角色，代理商拓展市场可通过管理平台“创建销售”方式完成。</p><br><p>爱码付团队</p><p>2017年08月28日</p>"); // 设置邮件的正文

			// mb.attachFile("F:/commons-collections-3.1.jar"); // 往邮件中添加附件
			// mb.attachFile("F:/c3p0-0.9.1.2.jar");
			// mb.attachFile("F:/副本.jar");

			SendMail sm = new SendMail();
			System.out.println("正在发送邮件..." + youxiang);

			if (sm.sendMail(mb)) // 发送邮件
				System.out.println("发送成功!");
			else
				System.out.println("发送失败!");
		}
		

	}
}
