package com.dxpj.zp.server;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dxpj.model.user_pass;
import com.dxpj.util.Code;
import com.dxpj.util.DataFormat;
import com.dxpj.util.DateUtils;
import com.dxpj.util.GetNumOfcomputer;
import com.dxpj.util.JsonMsg;
import com.dxpj.util.TtsMain;
import com.dxpj.zp.model.PartyBranch;
import com.dxpj.zp.model.PartyBranch_VideoInfo;
import com.dxpj.zp.model.PartyMembers;
import com.dxpj.zp.model.PartySpiritCheck_ActivityInfo;
import com.dxpj.zp.model.Q_SelfAssess_Answer_Replay;
import com.dxpj.zp.model.Q_SelfAssess_Question;
import com.dxpj.zp.model.Q_SelfAssess_QuestionnaireType;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * 
 * 注意的坑：1、如果没有传入答案，则答案默认为0
 *       2、需要判断该用户是第一次评价还是继续上次的任务继续评价，然后还得判断是往记录表中插入数据还是更新数据、
 * 
 * 
 * 
 */

/**
 * <p>
 * 党性自评服务层
 * 
 * @author huoyan
 *
 */
public class ZPserver {
	/**
	 * 活动信息对象<br>
	 * 其中上级党组织id目前只能是029-0001
	 * 
	 */
	PartySpiritCheck_ActivityInfo p_ActivityInfo;
	
	
	DataFormat dateFormat = new DataFormat();
	TtsMain tts;
     
	private static PartyBranch_VideoInfo partyBranch_VideoInfo;

	private static String parentPartyId_static , // 上级组织id
			partyType_static, // 党派类型名称
			partyMemberId_static, // 在党支部内部的党员id
			partyBranchId__static, // 党支部id
			ActivityId_static , // 活动id
	        partyMemberPass_static,// 在党支部内部的党员密码
	        assessType = "党性互评端",
	        pass;
	
	
	//AssesingTypeName     A_description
	//党性自评端
	//渭阳西路街道党员党性教育体检中心
		public JSONObject get_A_Description(String id_parentParty) {
			parentPartyId_static = id_parentParty;
			JSONObject jsonObject = new JSONObject(true);
			String sql= "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyBranch_VideoInfo] WHERE AssesingTypeID = '01' AND ParentID = ?";
			partyBranch_VideoInfo = PartyBranch_VideoInfo.dao.findFirst(sql, parentPartyId_static);
			if (partyBranch_VideoInfo!=null) {
				jsonObject.put("status", new JsonMsg());
				jsonObject.put("info_AssesingTypeName", partyBranch_VideoInfo.getStr("AssesingTypeName"));//党性自评端
				jsonObject.put("info_A_description", partyBranch_VideoInfo.getStr("A_description"));//渭阳西路街道党员党性教育体检中心
				return jsonObject;
			}
			jsonObject.put("status",new JsonMsg(Code.C_111));
			return jsonObject; 
		}
	/**
	 * <p>
	 * 判断是否存在活动<br>
	 * 
	 * 根据上级党组织id，判断在该组织下，是否有已经激活的活动<br>
	 * 其中上级党组织id目前只能是029-0001
	 * 
	 * @param id_parentParty
	 *            上级党组织id
	 * @return JsonMsg 判断结果
	 */
	public JsonMsg judge_IsActivity(String id_parentParty) {
		parentPartyId_static = id_parentParty;
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartySpiritCheck_ActivityInfo] WHERE IsValid = '1' AND ParentID = ?";
		p_ActivityInfo = PartySpiritCheck_ActivityInfo.dao.findFirst(sql, parentPartyId_static);// 获得已经激活的活动集合
		if (p_ActivityInfo != null && p_ActivityInfo.getInt("IsValid") == 1) {
			ActivityId_static = p_ActivityInfo.getStr("Activity_ID");
			return new JsonMsg();
		} else {
			return new JsonMsg(Code.C_102);
		}

	}
	
	
	//GeneralDescription
	//不忘初心、牢记使命，增强党性、提升素养。欢迎您来到渭阳西路街道党员党性教育体检中心，我是本次 “党性体检”语音提示小助手，本次党性体检流程由“党员自评”“党员互评”“群众评议”“组织评议”四个环节组成。
	public JSONObject get_generalDescription() {
		JSONObject jsonObject = new JSONObject(true);
		if (partyBranch_VideoInfo!=null) {
			jsonObject.put("status", new JsonMsg());
			jsonObject.put("info_AssesingTypeName", partyBranch_VideoInfo.getStr("GeneralDescription"));
			return jsonObject;
		}
		jsonObject.put("status",new JsonMsg(Code.C_111));
		return jsonObject; 
	}

	/**
	 * <p>
	 * 返回具有党性评价资格的党支部集合
	 * ParentID
	 * @return jsonObject 党支部集合
	 */
	public JSONObject get_party_selected() {

		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyBranch] WHERE IsValid = '1' AND ParentID = ? order by DisplayOrder";
		List<PartyBranch> list = PartyBranch.dao.find(sql, parentPartyId_static);
		JSONObject jsonObject = new JSONObject(true);

		if (list.size() > 0) {
			for (PartyBranch partyBranch : list) {
				partyBranch.remove("ParentID");
				partyBranch.remove("PW_OrgeAssess");
				partyBranch.remove("IsOrganizeAssess");
				partyBranch.remove("IsValid");
				partyBranch.remove("DisplayOrder");
			}

			jsonObject.put("status", new JsonMsg());
			jsonObject.put("info", list);
			jsonObject.put("info_A_input", partyBranch_VideoInfo.getStr("A_input"));//欢迎您来到党性体检第一环节“党员自评”,请选择您所在的支部
			return jsonObject;
		} else {
			jsonObject.put("status", new JsonMsg(Code.C_103));
			return jsonObject;
		}

	}

	/**
	 * <p>
	 * 将所有有资格参加自评的党支部进行分页操作
	 * 
	 * @param pageNumber
	 *            当前页
	 * @param pageSize
	 *            每页党支部数量
	 * @return page 分页结果
	 */
	public Page<PartyBranch> paginate_party(int pageNumber, int pageSize) {
		return PartyBranch.dao.paginate(pageNumber, pageSize, "SELECT *",
				"FROM [DB_PartySpritCheck_Test].[dbo].[PartyBranch] WHERE IsValid = '1' AND ParentID = '"
						+ parentPartyId_static + "'");
	}

	/**
	 * <p>
	 * 获得党员集合<br>
	 * 
	 * 通过党支部id获得有资格进行党性自评的党员集合
	 * 
	 * @param id_partyBranch
	 *            党支部id
	 * @return jsonObject 党员集合
	 */
	public JSONObject get_partyMember_byPartyId(String id_partyBranch) {
		partyBranchId__static = id_partyBranch;
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyMembers] WHERE PartyBranchID = ? AND ParentID = ? AND IsParticipatePartySpiritCheck = '1' order by rowNumber";
		List<PartyMembers> list = PartyMembers.dao.find(sql, partyBranchId__static, parentPartyId_static);
		JSONObject jsonObject = new JSONObject(true);
		if (list.size() > 0) {
			for (PartyMembers partyMembers : list) {
				partyMembers.set("Birthday", DateUtils.simDateToStr(partyMembers.get("Birthday")));
				partyMembers.remove("rowNumber");
				partyMembers.remove("ParentID");
				partyMembers.remove("PartyBranchID");
				partyMembers.remove("JoinPartyTime");
				partyMembers.remove("WorkUnit");
				partyMembers.remove("PartyPosition");
				partyMembers.remove("PartyMemberLableID");
				partyMembers.remove("IsParticipatePartySpiritCheck");
				partyMembers.remove("PW_PartySpiritCheck_SelfAssess");
				partyMembers.remove("IsSelfAssessment");
				partyMembers.remove("PW_PartySpiritCheck_MutualAssess");
				partyMembers.remove("IsMutualAssess");
				partyMembers.remove("PW_Login");
				partyMembers.remove("IsValid");
				partyMembers.remove("Note");
				partyMembers.remove("IsSecretary");
				partyMembers.remove("DisplayOrder");
				partyMembers.remove("PartyGroup");
			}
			jsonObject.put("status", new JsonMsg());
			jsonObject.put("info", list);
			jsonObject.put("info_B_determine", partyBranch_VideoInfo.getStr("B_determine"));//选择完成，请选择您的姓名
			jsonObject.put("info_C_type", partyBranch_VideoInfo.getStr("C_type"));//请选择党员类别
			jsonObject.put("info_C_pass", partyBranch_VideoInfo.getStr("C_pass"));//请输入登陆密码
			return jsonObject;
		} else {
			jsonObject.put("status", new JsonMsg(Code.C_104));
			return jsonObject;
		}
	}

	/**
	 * <p>
	 * 将支部内的成员进行分页操作
	 * 
	 * @param pageNumber
	 *            当前页
	 * @param pageSize
	 *            每页党员数量
	 * @param id_partyBranch
	 *            党支部id
	 * @return page 分页结果
	 */
	public Page<PartyMembers> paginate_member(int pageNumber, int pageSize, String id_partyBranch) {
		return PartyMembers.dao.paginate(pageNumber, pageSize, "SELECT *",
				"FROM [DB_PartySpritCheck_Test].[dbo].[PartyMembers] WHERE PartyBranchID = '" + id_partyBranch
						+ "' AND ParentID = '" + parentPartyId_static + "'");
	}

	/**
	 * <p>
	 * 检验登录用户的信息<br>
	 *
	 * 根据上级党组织id、党支部内部的党员id、党支部id、党支部所属的党派、 党员密码进行登录验证，<br>
	 * 其中上级党组织id目前只能是029-0001
	 * 
	 * @param id_partyMember
	 *            党支部内部的党员id
	 * @param partyType
	 *            党支部所属的党派
	 * @param pass
	 *            党员密码
	 * 
	 * @return jsonObject 验证结果
	 */
	public JSONObject judge_partyMember_byMemberId(String id_partyMember, String partyType, String pass) {
		partyType_static = partyType;
		partyMemberId_static = id_partyMember;
		partyMemberPass_static = pass;
		JSONObject jsonObject = new JSONObject(true);
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyMembers] WHERE ParentID =? AND PartyMemberID = ? AND PartyBranchID = ? AND PartyMemberLableName = ? AND PW_PartySpiritCheck_SelfAssess = ?";
		PartyMembers partyMember = PartyMembers.dao.findFirst(sql, parentPartyId_static, partyMemberId_static,
				partyBranchId__static, partyType_static, partyMemberPass_static);
		if (partyMember != null) {
			if (partyMember.getInt("IsSelfAssessment") == 0) {// 包含没有彻底完成自评时的两种状态：1、以前没有评价过；2、评价过，但是没有评价完成

				jsonObject.put("status", new JsonMsg());
			} else if (partyMember.getInt("IsSelfAssessment") == 2) {
				jsonObject.put("status", new JsonMsg(Code.C_105));
			}
		} else {
			jsonObject.put("status", new JsonMsg(Code.C_106));

		}
		return jsonObject;
	}

	/**
	 * <p>
	 * 发送问题和相关信息集合<br>
	 *
	 * 根据上级党组织id、党支部内部的党员id、党支部id、党支部所属的党派、 党员密码进行用户获取，<br>
	 * 其中上级党组织id目前只能是029-0001
	 * 
	 * @return jsonObject 信息集合（包含问题、个人信息、有自评记录的话，也会返回自评记录）
	 */

	public JSONObject get_questionAndAnswer() {

		JSONObject jsonObject = new JSONObject(true);
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyMembers] WHERE ParentID =? AND PartyMemberID = ? AND PartyBranchID = ? AND PartyMemberLableName = ? AND PW_PartySpiritCheck_SelfAssess = ?";
		PartyMembers partyMember = PartyMembers.dao.findFirst(sql, parentPartyId_static, partyMemberId_static,
				partyBranchId__static, partyType_static, partyMemberPass_static);
		
		
		partyMember.remove("rowNumber");
		partyMember.remove("ParentID");
		partyMember.remove("PartyBranchID");
		partyMember.remove("Sex");
		partyMember.remove("Birthday");
		partyMember.remove("JoinPartyTime");
		partyMember.remove("WorkUnit");
		partyMember.remove("PartyPosition");
		partyMember.remove("PartyMemberLableID");
		partyMember.remove("IsParticipatePartySpiritCheck");
		partyMember.remove("PW_PartySpiritCheck_SelfAssess");
		partyMember.remove("IsSelfAssessment");
		partyMember.remove("PW_PartySpiritCheck_MutualAssess");
		partyMember.remove("IsMutualAssess");
		partyMember.remove("PW_Login");
		partyMember.remove("IsValid");
		partyMember.remove("Note");
		partyMember.remove("IsSecretary");
		partyMember.remove("DisplayOrder");
		partyMember.remove("PartyGroup");

		List<Q_SelfAssess_Question> list = questionCollectionByPartyName(partyType_static);// 得到问题集合；
		for (Q_SelfAssess_Question q_SelfAssess_Question : list) {
			q_SelfAssess_Question.remove("QuestionnaireTypeID");
			q_SelfAssess_Question.remove("IsUse");
		}
		// 判断之前是否是有过记录(未完成提交那种)
		String sql1 = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[Q_SelfAssess_Answer_Replay] WHERE  ParentID = ? AND PartyMemberID_Assessed = ? AND PartyBranchID = ?  AND Activity_ID = ? AND IsFinish = '0'";
		Q_SelfAssess_Answer_Replay replay = Q_SelfAssess_Answer_Replay.dao.findFirst(sql1, parentPartyId_static,
				partyMemberId_static, partyBranchId__static, ActivityId_static);

		if (replay != null) {
			String str = replay.get("SelectedChoiceScoreStr").toString();
			JSONObject jsonObject2 = new JSONObject(true);
			jsonObject2 = JSON.parseObject(str);
			if (jsonObject2 != null) {
				jsonObject2.remove("Birthday");
				jsonObject2.remove("IsFinished");
				jsonObject2.remove("IsMutualAssess");
				jsonObject2.remove("IsParticipatePartySpiritCheck");
				jsonObject2.remove("IsSecretary");

				jsonObject2.remove("IsSelfAssessment");
				jsonObject2.remove("IsValid");
				jsonObject2.remove("Name");
				jsonObject2.remove("Note");
				jsonObject2.remove("PW_Login");
				jsonObject2.remove("PW_PartySpiritCheck_SelfAssess");
				jsonObject2.remove("PartyBranchID");

				jsonObject2.remove("PartyBranchName");
				jsonObject2.remove("PartyMemberID");
				jsonObject2.remove("PartyMemberLableID");
				jsonObject2.remove("PartyMemberLableName");
				jsonObject2.remove("PartyMemberNo");
				jsonObject2.remove("PartyPosition");
				jsonObject2.remove("QuestionNum");
				jsonObject2.remove("Sex");
				jsonObject2.remove("WorkUnit");
				jsonObject2.remove("rowNumber");
				
				System.out.println("wangjia::::::::::::::"+jsonObject2);
				
				
				
			}

			jsonObject.put("status", new JsonMsg());
			jsonObject.put("isHavingWrited", true);// 有信息记录
			jsonObject.put("info_question", list); // 添加答案
			jsonObject.put("info_answer", jsonObject2);// 添加问题和成员集合
			jsonObject.put("info_member", partyMember);

		} else {
			jsonObject.put("status", new JsonMsg());
			jsonObject.put("isHavingWrited", false);// 没有信息记录
			jsonObject.put("info_question", list); // 添加答案
			jsonObject.put("info_answer", false);
			jsonObject.put("info_member", partyMember);// 添加成员信息

		}
		jsonObject.put("info_C_determine", partyBranch_VideoInfo.getStr("C_determine"));//自评问卷由20道测试题组成，请您在每道题的答案中，选择与自身实际相符的答案，现在答题开始
		jsonObject.put("info_D_all_assessed", partyBranch_VideoInfo.getStr("D_all_assessed"));
		jsonObject.put("info_D_all_determine", partyBranch_VideoInfo.getStr("D_all_determine"));
		return jsonObject;
	}

	/**
	 * <p>
	 * 通过党派类别名称，获得问题类别id
	 * 
	 * @param partyTypeName
	 *            党派类别名称
	 * @return partyTypeId 问题类别id
	 */
	public String getQuestionnaireTypeId(String partyTypeName) {
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[Q_SelfAssess_QuestionnaireType] WHERE  QuestionnaireTypeName = ? ";
		Q_SelfAssess_QuestionnaireType q_SelfAssess_QuestionnaireType = Q_SelfAssess_QuestionnaireType.dao
				.findFirst(sql, partyTypeName);

		return q_SelfAssess_QuestionnaireType.getStr("QuestionnaireTypeID");

	}

	/**
	 * <p>
	 * 通过党派类别获得问题集合<br>
	 * 不同类别的党员，所回答的问题种类不同
	 *
	 * @param questionnaireTypeName
	 *            党员种类
	 * @return list 问题集合
	 */
	public List<Q_SelfAssess_Question> questionCollectionByPartyName(String questionnaireTypeName) {
		String sql_type = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[Q_SelfAssess_QuestionnaireType] WHERE QuestionnaireTypeName = ? ";
		Q_SelfAssess_QuestionnaireType q_SelfAssess_QuestionnaireType = Q_SelfAssess_QuestionnaireType.dao
				.findFirst(sql_type, questionnaireTypeName);
		List<Q_SelfAssess_Question> list = questionCollectionbyTypeId(
				q_SelfAssess_QuestionnaireType.get("QuestionnaireTypeID"));
		return list;

	}

	/**
	 * <p>
	 * 通过党派Id获得问题集合<br>
	 * 不同类别的党员，回答的问题不同
	 * 
	 * @param questionnaireTypeId
	 *            党员种类id
	 * @return list 问题集合
	 */
	public List<Q_SelfAssess_Question> questionCollectionbyTypeId(String questionnaireTypeId) {
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[Q_SelfAssess_Question] WHERE QuestionnaireTypeID = ? AND IsUse = '1' order by DisplayOder";
		List<Q_SelfAssess_Question> list = Q_SelfAssess_Question.dao.find(sql, questionnaireTypeId);
		return list;

	}

	/**
	 * <p>
	 * 发送问题集合和个人信息<br>
	 * 
	 * 通过党派类别得到问题集合和相应的附加信息，并发送给前端
	 * 
	 * @param questionnaireTypeName
	 *            党派类别名称
	 * @return jsonObject 问题和相应的附加信息、
	 *//*
		 * public JSONObject get_question_byPartyTypeName(String
		 * questionnaireTypeName) {
		 * 
		 * List<Q_SelfAssess_Question> list =
		 * questionCollectionByPartyName(questionnaireTypeName); JSONObject
		 * jsonObject = new JSONObject(true); if (list.size() > 0) {
		 * jsonObject.put("status", new JsonMsg()); jsonObject.put("info",
		 * list); return jsonObject; } else { jsonObject.put("status", new
		 * JsonMsg(Code.C_101)); return jsonObject; } }
		 */
	/*
	 * {
	 * 
	 * "IsFinished": 1, "Answer_0": "4", "Answer_1": "4", "Answer_2": "4",
	 * "Answer_3": "4", "Answer_4": "4", "Answer_5": "4", "Answer_6": "3",
	 * "Answer_7": "4", "Answer_8": "4", "Answer_9": "4", "Answer_10": "4",
	 * "Answer_11": "4", "Answer_12": "4", "Answer_13": "3", "Answer_14": "4",
	 * "Answer_15": "3", "Answer_16": "4", "Answer_17": "4", "Answer_18": "3",
	 * "Answer_19": "4" }
	 */
	/**
	 * <p>
	 * 保存已经提交的答案<br>
	 * 把前端传来的Json解析并将成绩简单计算，将计算结果和个人信息存入数据库
	 * 
	 * @param s
	 *            提交的答案
	 * @return jsonMsg 状态集合
	 */
	public JsonMsg save_questionAndAnswer(JSONObject s) {

		List<Q_SelfAssess_Question> list = questionCollectionByPartyName(partyType_static);
		System.out.println("list:" + list);

		JSONObject json = new JSONObject(true);
		json = s;// 获取答题结果
		System.out.println("IsFinished" + json.get("IsFinished"));

		// 获得该党员详细信息，用于存储到其他库
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyMembers] WHERE ParentID = ? AND PartyMemberID = ? AND PartyBranchID = ?";
		PartyMembers partyMember = PartyMembers.dao.findFirst(sql, parentPartyId_static, partyMemberId_static,
				partyBranchId__static);

		System.out.println("partyMember:" + partyMember);

		Date date = (partyMember.get("Birthday"));
		String bir = DateUtils.dateToStr(date);

		// 计算自评结果，存储到sum中;
		int sum_self = 0;
		for (int i = 0; i < list.size(); i++) {
			String answer = "Answer_" + i;
			// String question = "Question_"+i;
			try {
				sum_self = Integer.parseInt((json.getString(answer).equals("")) ? "0" : json.getString(answer))
						+ sum_self;
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			// json.put(question, list.get())

		}

		json.put("PartyMemberNo", 0);
		json.put("QuestionNum", list.size());
		json.put("rowNumber", partyMember.get("rowNumber"));
		json.put("PartyBranchID", partyBranchId__static);
		json.put("PartyMemberID", partyMemberId_static);
		json.put("Name", partyMember.get("Name"));
		json.put("Sex", partyMember.get("Sex"));
		json.put("Birthday", bir);
		
		json.put("PartyBranchName", partyMember.get("PartyBranchName"));
		json.put("WorkUnit", partyMember.get("WorkUnit"));
		json.put("PartyPosition", partyMember.get("PartyPosition"));
		json.put("PartyMemberLableID", partyMember.get("PartyMemberLableID"));
		json.put("PartyMemberLableName", partyMember.get("PartyMemberLableName"));
		json.put("IsParticipatePartySpiritCheck", partyMember.get("IsParticipatePartySpiritCheck"));
		json.put("PW_PartySpiritCheck_SelfAssess", partyMember.get("PW_PartySpiritCheck_SelfAssess"));
		json.put("IsSelfAssessment", partyMember.get("IsSelfAssessment"));
		json.put("IsMutualAssess", partyMember.get("IsMutualAssess"));
		json.put("PW_Login", partyMember.get("PW_Login"));
		json.put("IsValid", partyMember.get("IsValid"));
		json.put("Note", partyMember.get("Note"));
		json.put("IsSecretary", partyMember.get("IsSecretary"));

		// 计算总分数值totalScore
		// String sql_sum = "SELECT * FROM
		// [DB_PartySpritCheck_Test].[dbo].[PartySpiritCheck_SumAssesment] WHERE
		// ParentID = ? AND PartyBranchID = ? AND Activity_ID = ? AND
		// PartyMemberID_Assessed = ?";
		// PartySpiritCheck_SumAssesment SumAssesment =
		// PartySpiritCheck_SumAssesment.dao.findFirst(sql_sum,
		// parentPartyId_static, partyBranchId__static, ActivityId_static,
		// partyMemberId_static);
		// System.out.println("SumAssesment:" + SumAssesment);
		// double totalScore = sum_self * 0.2 +
		// SumAssesment.getFloat("MutualAssessScore") * 0.25
		// + SumAssesment.getFloat("MassAssessScore") * 0.25 +
		// SumAssesment.getFloat("OrgAssessScore") * 0.3;
		// System.out.println("totalScore:" +
		// dateFormat.doubleToTwo(totalScore));

		// 用于判断Q_SelfAssess_Answer_Replay中是否存在用户之前评价的记录，如果有，则只需要更相应的值，否则，则需要插入新的一条数据记录
		String sql_Replay = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[Q_SelfAssess_Answer_Replay] WHERE ParentID = ? AND PartyBranchID = ? AND Activity_ID = ? AND PartyMemberID_Assessed = ?";
		Q_SelfAssess_Answer_Replay Answer_Replay = Q_SelfAssess_Answer_Replay.dao.findFirst(sql_Replay,
				parentPartyId_static, partyBranchId__static, ActivityId_static, partyMemberId_static);

		int isSelfAssessment, sum_self1 = sum_self;
		if (json.get("IsFinished").equals(0))
			isSelfAssessment = 0;
		else
			isSelfAssessment = 2;
		JsonMsg jsonMsg = null;
		JSONObject json_copy = json;
		if (Answer_Replay != null) {// 表Q_SelfAssess_Answer_Replay中已存在上次的未完成的数据，所以，这次只能更新Q_SelfAssess_Answer_Replay表中的数据
			// 事务操作
			boolean succeed = Db.tx(new IAtom() {
				public boolean run() throws SQLException {
					int
					// count = 0,
					count1 = 0, flag = 0;
					if (json_copy.get("IsFinished").equals(1)) {
						flag = 1;
						// 更新成绩表
						// count = Db.update(
						// "update
						// [DB_PartySpritCheck_Test].[dbo].[PartySpiritCheck_SumAssesment]
						// SET SelfAssessmentScore = ?,TotalScore = ? WHERE
						// ParentID = ? AND PartyBranchID = ? AND Activity_ID =
						// ? AND PartyMemberID_Assessed = ?",
						//
						// sum_self1, dateFormat.doubleToTwo(totalScore),
						// parentPartyId_static, partyBranchId__static,
						// ActivityId_static, partyMemberId_static);
						// 更新用户表
						count1 = Db.update(
								"update  [DB_PartySpritCheck_Test].[dbo].[PartyMembers] SET IsSelfAssessment = ? WHERE ParentID = ? AND PartyMemberID = ? AND PartyBranchID = ?",
								isSelfAssessment, parentPartyId_static, partyMemberId_static, partyBranchId__static);
						// 更新到答案表
					}
					int count2 = Db.update(
							"update  [DB_PartySpritCheck_Test].[dbo].[Q_SelfAssess_Answer_Replay] SET AssessingPW = ?,SelectedChoiceScoreStr = ?,SumScore = ?,SubmitTime = ?,IsFinish = ? WHERE ParentID = ? AND PartyMemberID_Assessed = ? AND PartyBranchID = ? AND Activity_ID = ?",
							partyMember.get("PW_PartySpiritCheck_SelfAssess").toString(), json_copy.toString(),
							sum_self1, DateUtils.getNowDateTimeStr(), json_copy.get("IsFinished"), parentPartyId_static,
							partyMemberId_static, partyBranchId__static, ActivityId_static);
					// System.out.println("count"+count);
					System.out.println("count1" + count1);
					System.out.println("count2" + count2);
					// // 保存到答案表
					// boolean count2 = Db.save("Q_SelfAssess_Answer_Replay",
					// q_SelfAssess_Answer_Replay);
					if (flag == 1) {
						return count1 == 1 && count2 == 1;
					}
					return count2 == 1;

				}
			});

			if (succeed) {
				jsonMsg = new JsonMsg();
			} else {
				return (new JsonMsg(Code.C_107));
			}

		} else {// 从未在此活动中自评过，所以需要插入数据
				// 用于插入到表中
			Record q_SelfAssess_Answer_Replay = new Record();
			q_SelfAssess_Answer_Replay.set("Activity_ID", ActivityId_static);
			q_SelfAssess_Answer_Replay.set("PartyBranchID", partyBranchId__static);
			q_SelfAssess_Answer_Replay.set("PartyMemberID_Assessed", partyMemberId_static);
			q_SelfAssess_Answer_Replay.set("QuestionnaireTypeID", getQuestionnaireTypeId(partyType_static));
			q_SelfAssess_Answer_Replay.set("AssessingPW", partyMember.get("PW_PartySpiritCheck_SelfAssess").toString());
			q_SelfAssess_Answer_Replay.set("SelectedChoiceScoreStr", json.toString());
			q_SelfAssess_Answer_Replay.set("SumScore", sum_self);
			q_SelfAssess_Answer_Replay.set("SubmitTime", DateUtils.getNowDateTimeStr());
			q_SelfAssess_Answer_Replay.set("IsFinish", json.get("IsFinished"));
			q_SelfAssess_Answer_Replay.set("ParentID", parentPartyId_static);
			System.out.println("q_SelfAssess_Answer_Replay_null:" + q_SelfAssess_Answer_Replay);
			// 事务操作
			boolean succeed = Db.tx(new IAtom() {
				public boolean run() throws SQLException {
					int
					// count = 0,
					count1 = 0, flag = 0;
					if (json_copy.getBoolean("IsFinished")) {
						flag = 1;
						// 更新成绩表
						// count = Db.update(
						// "update
						// [DB_PartySpritCheck_Test].[dbo].[PartySpiritCheck_SumAssesment]
						// SET SelfAssessmentScore = ?,TotalScore = ? WHERE
						// ParentID = ? AND PartyBranchID = ? AND Activity_ID =
						// ? AND PartyMemberID_Assessed = ?",
						//
						// sum_self1, dateFormat.doubleToTwo(totalScore),
						// parentPartyId_static,
						// partyBranchId__static, ActivityId_static,
						// partyMemberId_static);
						// 更新用户表
						count1 = Db.update(
								"update  [DB_PartySpritCheck_Test].[dbo].[PartyMembers] SET IsSelfAssessment = ? WHERE ParentID = ? AND PartyMemberID = ? AND PartyBranchID = ?",
								isSelfAssessment, parentPartyId_static, partyMemberId_static, partyBranchId__static);
					}
					// 保存到答案表
					boolean count2 = Db.save("Q_SelfAssess_Answer_Replay", q_SelfAssess_Answer_Replay);
					if (flag == 1) {
						return count1 == 1 && count2 == true;
					}
					return count2 == true;
				}
			});
			if (succeed) {
				jsonMsg = new JsonMsg();
			} else {
				return (new JsonMsg(Code.C_107));
			}
		}
		return jsonMsg;

	}

	@SuppressWarnings("unused")
	private ClassLoader getClassLoader() {
		ClassLoader ret = Thread.currentThread().getContextClassLoader();
		return ret != null ? ret : getClass().getClassLoader();
	}

	/**
	 * <p>
	 * 对文件内容进行播音提示<br>
	 * 根据前端返回的文件名前缀，对文件内容进行播音提示
	 * 
	 * @param fileName
	 *            选择提示音文件
	 * @return jsonMsg 状态
	 * @throws IOException
	 *             异常
	 *//*
		 * public JsonMsg video_file(String fileName) throws IOException {
		 * ActiveXComponent sap = new ActiveXComponent("Sapi.SpVoice"); JsonMsg
		 * jsonMsg = new JsonMsg(); // 获取执行对象 Dispatch sapo = sap.getObject();
		 * // 输入文件 InputStream inputStream =
		 * getClassLoader().getResourceAsStream(fileName); if (inputStream ==
		 * null) { throw new
		 * IllegalArgumentException(" file not found in classpath: " +
		 * "zs.txt"); } InputStreamReader inputStreamReader = new
		 * InputStreamReader(inputStream, "GBK"); // 使用包装字符流读取文件 BufferedReader
		 * br = new BufferedReader(inputStreamReader); String content =
		 * br.readLine();
		 * 
		 * try { // 音量 0-100 sap.setProperty("Volume", new Variant(100)); //
		 * 语音朗读速度 -10 到 +10 sap.setProperty("Rate", new Variant(-1)); while
		 * (content != null) { Dispatch.call(sapo, "Speak", new
		 * Variant(content)); content = br.readLine(); } jsonMsg = new
		 * JsonMsg(); } catch (Exception e) { jsonMsg = new JsonMsg(Code.C_101);
		 * System.out.println(e); e.printStackTrace(); } finally { br.close();
		 * inputStreamReader.close(); inputStream.close(); // 关闭应用程序连接
		 * sapo.safeRelease(); sap.safeRelease(); } return jsonMsg; }
		 */
	/**
	 * <p>
	 * 自定义播音提示<br>
	 * 自己输入简短的文字， 进行播音提示
	 * 
	 * @param word
	 *            自定义提示文字
	 * @return jsonMsg 返回的状态
	 * @throws Exception 
	 *//*
		 * public JsonMsg video_word(String word) { JsonMsg jsonMsg = new
		 * JsonMsg(); ActiveXComponent sap = new
		 * ActiveXComponent("Sapi.SpVoice"); try { // 音量 0-100
		 * sap.setProperty("Volume", new Variant(100)); // 语音朗读速度 -10 到 +10
		 * sap.setProperty("Rate", new Variant(0)); // 获取执行对象 Dispatch sapo =
		 * sap.getObject(); // 执行朗读 Dispatch.call(sapo, "Speak", new
		 * Variant(word)); // 关闭执行对象 sapo.safeRelease(); jsonMsg = new
		 * JsonMsg(); } catch (Exception e) { jsonMsg = new JsonMsg(Code.C_101);
		 * e.printStackTrace(); } finally { // 关闭应用程序连接 sap.safeRelease(); }
		 * return jsonMsg; }
		 */
	//Url:
	 public JSONObject video_words(JSONObject s) throws Exception{
//		 System.out.println("words:"+s.getString("words"));
//		 System.out.println("parentPartyId_static:"+parentPartyId_static);
		 return TtsMain.tts1(parentPartyId_static, "党性自评端", s.getString("words"));
//		 return null;
//		 return TtsMain.getMP3();
//		 getMP3
		 
	 }


	
	 
	
	

	
	}
