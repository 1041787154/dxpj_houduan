package com.dxpj.config;


import com.dxpj.hp.controller.HPController;
import com.dxpj.hp.model.Q_MutualAssess_Answer_Replay;
import com.dxpj.hp.model.Q_MutualAssess_Question;
import com.dxpj.hp.model.RedLine;
import com.dxpj.model.PartyBranch_Wav;
import com.dxpj.model.backGround;
import com.dxpj.model.partyBranch_Wav_MP3;
import com.dxpj.model.user_pass;
import com.dxpj.qzp.controller.QZPController;
import com.dxpj.qzp.model.MassAssessment_User;
import com.dxpj.qzp.model.Q_MassAssess_Answer_Replay;
import com.dxpj.qzp.model.Q_MassAssess_Question;
import com.dxpj.zp.controller.ZPController;
import com.dxpj.zp.model.PartyBranch;
import com.dxpj.zp.model.PartyBranch_VideoInfo;
import com.dxpj.zp.model.PartyMembers;
import com.dxpj.zp.model.PartySpiritCheck_ActivityInfo;
import com.dxpj.zp.model.PartySpiritCheck_SumAssesment;
import com.dxpj.zp.model.Q_SelfAssess_Answer_Replay;
import com.dxpj.zp.model.Q_SelfAssess_Question;
import com.dxpj.zp.model.Q_SelfAssess_QuestionnaireType;
import com.dxpj.zzp.controller.ZZPController;
import com.dxpj.zzp.model.Q_OrgAssess_Answer_Replay;
import com.dxpj.zzp.model.Q_OrgAssess_Question;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.ext.handler.ContextPathHandler;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.CaseInsensitiveContainerFactory;
import com.jfinal.plugin.activerecord.dialect.SqlServerDialect;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.render.ViewType;
import com.jfinal.template.Engine;


public class DXPJConfig extends JFinalConfig{
	/**
	 * 配置常量
	 */
	@Override
	public void configConstant(Constants me) {
		// 加载少量必要配置，随后可用PropKit.get(...)获取值
		PropKit.use("a_little_config.txt");//加载数据库
//		me.setViewType(ViewType.FREE_MARKER);
		me.setDevMode(true);  //// devMode 配置为 true，将支持模板实时热加载
		me.setViewType(ViewType.JSP);//视图类型
		me.setEncoding("utf-8");
		
	}

	/**
	 * 配置路由
	 */
	@Override
	public void configRoute(Routes me) {
//		me.add("/", ParentController.class,"/parent");
		me.add("/", ZPController.class,"/zp");	
		me.add("/zp", ZPController.class);		
		me.add("/hp", HPController.class);	
		me.add("/qzp", QZPController.class);		
		me.add("/zzp", ZZPController.class);
			
		
		
	}

	@Override
	public void configEngine(Engine me) {
		
	}

	/**
	 * 映射实体和数据库中的表格
	 */
	@Override
	public void configPlugin(Plugins me) {

		DruidPlugin druidPlugin = new DruidPlugin(PropKit.get("jdbcUrl"), PropKit.get("user"),
				PropKit.get("password").trim());
		druidPlugin.setDriverClass(PropKit.get("driver"));
		me.add(druidPlugin);
		
		// 配置ActiveRecord插件
		ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
		
		// 配置Sqlserver方言
		arp.setDialect(new SqlServerDialect());
		arp.setShowSql(true);
	
	
		// 配置属性名(字段名)大小写不敏感容器工厂
		arp.setContainerFactory(new CaseInsensitiveContainerFactory());
		me.add(arp);
		
		arp.addMapping("[DB_PartySpritCheck_Test].[dbo].[PartySpiritCheck_ActivityInfo]","Activity_ID,ParentID",PartySpiritCheck_ActivityInfo.class);
//		PartyBranch
		arp.addMapping("[DB_PartySpritCheck_Test].[dbo].[PartyBranch]","PartyBranchID,ParentID",PartyBranch.class);
//		PartyMembers
		arp.addMapping("[DB_PartySpritCheck_Test].[dbo].[PartyMembers]","ParentID,PartyBranchID,PartyMemberID,PartyMemberLableID,PartyMemberLableName",PartyMembers.class);
//	    Q_SelfAssess_Question
		arp.addMapping("[DB_PartySpritCheck_Test].[dbo].[Q_SelfAssess_Question]","QuestionID,QuestionnaireTypeID",Q_SelfAssess_Question.class);
//		Q_SelfAssess_Answer_Replay
		arp.addMapping("[DB_PartySpritCheck_Test].[dbo].[Q_SelfAssess_Answer_Replay]","Activity_ID,PartyBranchID,PartyMemberID_Assessed,QuestionnaireTypeID,ParentID",Q_SelfAssess_Answer_Replay.class);
//	    Q_SelfAssess_QuestionnaireType
		arp.addMapping("[DB_PartySpritCheck_Test].[dbo].[Q_SelfAssess_QuestionnaireType]","QuestionnaireTypeID,QuestionnaireTypeName",Q_SelfAssess_QuestionnaireType.class);
//		PartySpiritCheck_SumAssesment
		arp.addMapping("[DB_PartySpritCheck_Test].[dbo].[PartySpiritCheck_SumAssesment]","PartyBranchID,Activity_ID,PartyMemberID_Assessed",PartySpiritCheck_SumAssesment.class);
//		Q_MutualAssess_Question
		arp.addMapping("[DB_PartySpritCheck_Test].[dbo].[Q_MutualAssess_Question]","QuestionID",Q_MutualAssess_Question.class);
//		RedLine
		arp.addMapping("[DB_PartySpritCheck_Test].[dbo].[RedLine]","RedLineID",RedLine.class);
//		Q_MutualAssess_Answer_Replay
		arp.addMapping("[DB_PartySpritCheck_Test].[dbo].[Q_MutualAssess_Answer_Replay]","Activity_ID,PartyBranchID,PartyMemberID_Assessed,PartyMemberID_Assessing,ParentID",Q_MutualAssess_Answer_Replay.class);
//		MassAssessment_User
		arp.addMapping("[DB_PartySpritCheck_Test].[dbo].[MassAssessment_User]","PartyBranchID,LoginID,ParentID",MassAssessment_User.class);
//		Q_MassAssess_Question
		arp.addMapping("[DB_PartySpritCheck_Test].[dbo].[Q_MassAssess_Question]","QuestionID",Q_MassAssess_Question.class);
//		Q_MassAssess_Answer_Replay
		arp.addMapping("[DB_PartySpritCheck_Test].[dbo].[Q_MassAssess_Answer_Replay]","Activity_ID,PartyBranchID,PartyMemberID_Assessed,AssessingPW,ParentID",Q_MassAssess_Answer_Replay.class);
//		Q_OrgAssess_Question
		arp.addMapping("[DB_PartySpritCheck_Test].[dbo].[Q_OrgAssess_Question]","QuestionID",Q_OrgAssess_Question.class);
//		Q_OrgAssess_Answer_Replay
		arp.addMapping("[DB_PartySpritCheck_Test].[dbo].[Q_OrgAssess_Answer_Replay]","QuestionID",Q_OrgAssess_Answer_Replay.class);
//		PartyBranch_VideoInfo
		arp.addMapping("[DB_PartySpritCheck_Test].[dbo].[PartyBranch_VideoInfo]","ParentID,AssesingTypeID",PartyBranch_VideoInfo.class);
//		PartyBranch_ParentRank1
//		arp.addMapping("[DB_PartySpritCheck_Test].[dbo].[PartyBranch_ParentRank1]","PartyCommittee_ID",PartyBranch_ParentRank1.class);
//		PartyBranch_Wav
		arp.addMapping("[DB_PartySpritCheck_Test].[dbo].[PartyBranch_Wav]","voice_name",PartyBranch_Wav.class);
//		backGround
		arp.addMapping("[DB_PartySpritCheck_Test].[dbo].[backGround]","ParentID,PictureTypeID,AssessTypeID",backGround.class);
//		partyBranch_Wav_MP3
		arp.addMapping("[DB_PartySpritCheck_Test].[dbo].[partyBranch_Wav_MP3]","ParentID,AssessType,videoText",partyBranch_Wav_MP3.class);
//		user_pass
		arp.addMapping("[DB_PartySpritCheck_Test].[dbo].[user_pass]","id",user_pass.class);
	}

	@Override
	public void configInterceptor(Interceptors me) {
		
	}

	@Override
	public void configHandler(Handlers me) {
		me.add(new ContextPathHandler("contextPath"));//设置上下路径
	}

}
