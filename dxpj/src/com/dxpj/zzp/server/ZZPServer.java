package com.dxpj.zzp.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dxpj.hp.model.RedLine;
import com.dxpj.qzp.model.MassAssessment_User;
import com.dxpj.util.Code;
import com.dxpj.util.DataFormat;
import com.dxpj.util.DateUtils;
import com.dxpj.util.JsonMsg;
import com.dxpj.util.TtsMain;
import com.dxpj.zp.model.PartyBranch;
import com.dxpj.zp.model.PartyBranch_VideoInfo;
import com.dxpj.zp.model.PartyMembers;
import com.dxpj.zp.model.PartySpiritCheck_ActivityInfo;
import com.dxpj.zp.model.PartySpiritCheck_SumAssesment;
import com.dxpj.zp.model.Q_SelfAssess_Answer_Replay;
import com.dxpj.zzp.model.Q_OrgAssess_Answer_Replay;
import com.dxpj.zzp.model.Q_OrgAssess_Question;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.template.expr.ast.Field;

import sun.text.resources.cldr.lag.FormatData_lag;

public class ZZPServer {

	// 存在的问题：未将正在评价的群众用户的状态改为1
	// 校验用户时，未评价完成时的表中记录，Json的String未转化成Json
	/**
	 * 活动信息对象<br>
	 * 其中上级党组织id目前只能是029-0001
	 * 
	 */
	PartySpiritCheck_ActivityInfo p_ActivityInfo;
	DataFormat dataFormat = new DataFormat();
	private static PartyBranch_VideoInfo partyBranch_VideoInfo;
	
	private static String parentPartyId_static, // 上级组织id
			partyBranchId__static, // 党支部id
			activityId_static, // 活动id
			assessedId_static, 
			assessingPass_static;
	
	//AssesingTypeName     A_description
	//党性自评端
	//渭阳西路街道党员党性教育体检中心
		public JSONObject get_A_Description(String id_parentParty) {
			parentPartyId_static = id_parentParty;
			JSONObject jsonObject = new JSONObject(true);
			String sql= "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyBranch_VideoInfo] WHERE AssesingTypeID = '04' AND ParentID = ?";
			partyBranch_VideoInfo = PartyBranch_VideoInfo.dao.findFirst(sql, parentPartyId_static);
			if (partyBranch_VideoInfo!=null) {
				jsonObject.put("status", new JsonMsg());
				jsonObject.put("info_AssesingTypeName", partyBranch_VideoInfo.getStr("AssesingTypeName"));//党性群众评端
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
			activityId_static = p_ActivityInfo.getStr("Activity_ID");
			return new JsonMsg();
		} else {
			return new JsonMsg(Code.C_102);
		}

	}

	/**
	 * <p>
	 * 参加党性评议的党支部集合
	 * 
	 * 
	 * @return jsonObject 党支部集合
	 */
	public JSONObject get_party_selected() {

		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyBranch] WHERE IsValid = '1' AND ParentID = ?";
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
			jsonObject.put("info_A_input", partyBranch_VideoInfo.getStr("A_input"));//欢迎您来到党员党性体检 “组织评议”环节
			jsonObject.put("info_C_pass", partyBranch_VideoInfo.getStr("C_pass"));//选择完成，请输入登陆密码
			
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
	 * 检验登录组织的信息<br>
	 *
	 * 根据上级党组织id、党支部id、 组织密码进行登录验证，<br>
	 * 其中上级党组织id目前只能是029-0001
	 * 
	 * @param id_partyBranch
	 *            党支部id
	 * @param pass
	 *            组织密码
	 * 
	 * @return jsonObject 验证结果
	 */
	public JSONObject judge_partyMember_byMemberId(String id_partyBranch, String pass) {
		partyBranchId__static = id_partyBranch;
		assessingPass_static = pass;
		JSONObject jsonObject = new JSONObject(true);
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyBranch] WHERE ParentID =? AND PartyBranchID = ? AND PW_OrgeAssess = ?";
		PartyBranch orgeUser = PartyBranch.dao.findFirst(sql, parentPartyId_static, partyBranchId__static,
				assessingPass_static);

		// 个人信息表单
		String sql_member1 = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyMembers] WHERE ParentID =? AND PartyBranchID = ? AND IsParticipatePartySpiritCheck = '1'";// 默认取出党支部内的所有党员
		List<PartyMembers> list_member_noWrited = PartyMembers.dao.find(sql_member1, parentPartyId_static,
				partyBranchId__static);// 可以细化

		if (orgeUser != null) {
			// 尚未评价
			if (orgeUser.getInt("IsOrganizeAssess") == 0) {

				// 判断支部内是否有成员可以评价
				if (list_member_noWrited != null && list_member_noWrited.size() > 1) {// 不只包括自己
					jsonObject.put("status", new JsonMsg());
				} else {
					jsonObject.put("status", new JsonMsg(Code.C_108));// 无评价对象
				}
				// 没有评价完成
			} else if (orgeUser.getInt("IsOrganizeAssess") == 1) {

				// 从记录表中提交完整信息的记录个数
				String sql_member = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[Q_OrgAssess_Answer_Replay] WHERE ParentID =? AND PartyBranchID = ? AND Activity_ID = ? AND IsFinish = '1'";// 默认取出党支部内的所有党员
				List<Q_OrgAssess_Answer_Replay> list_member_beWrited = Q_OrgAssess_Answer_Replay.dao.find(sql_member,
						parentPartyId_static, partyBranchId__static, activityId_static);// 可以细化
				if (list_member_beWrited != null) {
					if (list_member_beWrited.size() == list_member_noWrited.size()) {
						jsonObject.put("status", new JsonMsg(Code.C_105));// 您已经完成提交，无需再次登录
					} else {
						jsonObject.put("status", new JsonMsg());
					}
				}

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
	 * 根据上级党组织id、党支部id、 组织密码进行登录验证，<br>
	 * 其中上级党组织id目前只能是029-0001
	 * 
	 * @return jsonObject 信息集合（包含问题、个人信息等）
	 */
	public JSONObject get_questionAndAnswer() {
		JSONObject jsonObject = new JSONObject(true);
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyBranch] WHERE ParentID =? AND PartyBranchID = ? AND PW_OrgeAssess = ?";
		PartyBranch orgeUser = PartyBranch.dao.findFirst(sql, parentPartyId_static, partyBranchId__static,
				assessingPass_static);

		// 答案表单
		List<Q_OrgAssess_Question> list_question = questionCollection();
		for (Q_OrgAssess_Question q_OrgAssess_Question : list_question) {
			q_OrgAssess_Question.remove("IsUse");
		}
		List<RedLine> list_redLine = redLineCollection();

		// 个人信息表单
		String sql_member1 = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyMembers] WHERE ParentID =? AND PartyBranchID = ? AND IsParticipatePartySpiritCheck = '1' order by rowNumber";// 默认取出党支部内的所有党员
		List<PartyMembers> list_member_noAssessed = PartyMembers.dao.find(sql_member1, parentPartyId_static,
				partyBranchId__static);// 可以细化
		List<PartyMembers> list_copy = list_member_noAssessed;
		// 尚未评价
		if (orgeUser.getInt("IsOrganizeAssess") == 0) {
			for (PartyMembers partyMembers : list_copy) {
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
//				partyMembers.remove("Note");
				partyMembers.set("Note","未完成");
				partyMembers.remove("IsSecretary");
				partyMembers.remove("DisplayOrder");
				partyMembers.remove("PartyGroup");
				
				
				
				
				
				
				
			}
			jsonObject.put("status", new JsonMsg());
			jsonObject.put("isAssessed",false);
			jsonObject.put("info_record", "0");
			jsonObject.put("list_member_Assessed", list_copy);			
			jsonObject.put("info_question", list_question);
			jsonObject.put("info_redLine", list_redLine);
			
			jsonObject.put("info_C_determine", partyBranch_VideoInfo.getStr("C_determine"));//请您依据党员实情，按照画面评议内容，逐一对您所在支部的其他党员予以赋分，“党员互评”开始，请选择你要评议的党员
			jsonObject.put("info_D_member", partyBranch_VideoInfo.getStr("D_member"));//请您在赋分完成后，点击“提交”按键
			
			jsonObject.put("info_D_question_assessing", partyBranch_VideoInfo.getStr("D_question_assessing"));//请完成没有作答的问题！
			jsonObject.put("info_D_question_assessed", partyBranch_VideoInfo.getStr("D_question_assessed"));//请进行红线问题作答
			
			jsonObject.put("info_D_redLine_assessing", partyBranch_VideoInfo.getStr("D_redLine_assessing"));//请完成没有作答的红线问题！
			jsonObject.put("info_D_redLine_assessed", partyBranch_VideoInfo.getStr("D_redLine_assessed"));//已完成评价，请点击“完成”按键完成评价过程。
			
			jsonObject.put("info_D_redLine_determine", partyBranch_VideoInfo.getStr("D_redLine_determine"));//请选择下一位您要评价的党员
			jsonObject.put("info_D_all_assessed", partyBranch_VideoInfo.getStr("D_all_assessed"));//所有评价已完成，请点击“提交”按键，提交评价数据。
			
			jsonObject.put("info_D_all_determine", partyBranch_VideoInfo.getStr("D_all_determine"));//null

			// 没有评价完成
		} else if (orgeUser.getInt("IsOrganizeAssess") == 1) {

			// 成绩表单
			String sql_member = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[Q_OrgAssess_Answer_Replay] WHERE ParentID =? AND PartyBranchID = ? AND Activity_ID = ?";// 记录信息
			List<Q_OrgAssess_Answer_Replay> list_member_assessed = Q_OrgAssess_Answer_Replay.dao.find(sql_member,
					parentPartyId_static, partyBranchId__static, activityId_static);// 可以细化

			String str;
			JSONArray jsonArray = new JSONArray();// 存放有评价记录的信息
			JSONObject jsonObject2 = new JSONObject(true);

			for (int i = 0; i < list_member_assessed.size(); i++) {
				str = list_member_assessed.get(i).getStr("SelectedChoiceScoreStr").toString();
				jsonObject2 = JSON.parseObject(str);//记录集合info_record
				
				if (jsonObject2 != null) {
					if(!jsonObject2.getBoolean("IsFinished")){//IsFinished:0
						
						jsonObject2.remove("PartyMemberNo");
						jsonObject2.remove("rowNumber");
						jsonObject2.remove("PartyBranchID");

						jsonObject2.remove("WorkUnit");
						jsonObject2.remove("PartyPosition");
						jsonObject2.remove("PartyMemberLableID");
						jsonObject2.remove("IsParticipatePartySpiritCheck");
						jsonObject2.remove("PW_PartySpiritCheck_SelfAssess");
						jsonObject2.remove("IsSelfAssessment");
						jsonObject2.remove("IsSelfAssessment");

						jsonObject2.remove("IsMutualAssess");
						jsonObject2.remove("PW_Login");
						jsonObject2.remove("IsValid")
						;
						jsonObject2.remove("QuestionNum");
						jsonObject2.remove("Sex");
						jsonObject2.remove("Birthday");
						jsonObject2.remove("Name");
						
						jsonObject2.remove("PartyMemberLableName");
						jsonObject2.remove("PartyBranchName");
						jsonObject2.remove("Note");
						jsonObject2.remove("IsFinished");
						jsonObject2.remove("IsSecretary");
						jsonObject2.remove("ScoreSum");
						jsonArray.add(jsonObject2);//记录答案的集合info_record
					}
					
				}	
				
				
			}
			System.out.println("============jsonArray=========:"+jsonArray);
			
			
			
//			PartyMembers partyMember;
//			Q_OrgAssess_Answer_Replay q_OrgAssess_Answer_Replay;
//			for (int i = 0; i < list_member_noAssessed.size(); i++) {
//				 partyMember = list_member_noAssessed.get(i);
//				for (int j = 0; j < list_member_assessed.size(); j++) {
//					 q_OrgAssess_Answer_Replay = list_member_assessed.get(j);
//					if (partyMember.getStr("PartyMemberID").equals(q_OrgAssess_Answer_Replay.getStr("PartyMemberID_Assessed"))) {						
//						list_member_noAssessed.remove(partyMember);
//					}
//				}	
//			}
//			for (PartyMembers partyMembers : list_member_noAssessed) {
//				partyMembers.remove("rowNumber");
//				partyMembers.remove("ParentID");
//				partyMembers.remove("PartyBranchID");
//				partyMembers.remove("JoinPartyTime");
//				partyMembers.remove("WorkUnit");
//				partyMembers.remove("PartyPosition");
//				partyMembers.remove("PartyMemberLableID");
//				partyMembers.remove("IsParticipatePartySpiritCheck");
//				partyMembers.remove("PW_PartySpiritCheck_SelfAssess");
//				partyMembers.remove("IsSelfAssessment");
//				partyMembers.remove("PW_PartySpiritCheck_MutualAssess");
//				partyMembers.remove("IsMutualAssess");
//				partyMembers.remove("PW_Login");
//				partyMembers.remove("IsValid");
//				partyMembers.set("Note","未完成");
////				partyMembers.remove("Note");
//				partyMembers.remove("IsSecretary");
//				partyMembers.remove("DisplayOrder");
//				partyMembers.remove("PartyGroup");
//			}
			JSONObject jsonObject3 = new JSONObject();
			for (PartyMembers partyMembers : list_member_noAssessed) {//成员
				partyMembers.set("Note", "未完成");
				for (int j = 0; j < list_member_assessed.size(); j++) {
					str = list_member_assessed.get(j).getStr("SelectedChoiceScoreStr").toString();
					jsonObject3 = JSON.parseObject(str);
					if(jsonObject3.get("PartyMemberID").equals(partyMembers.get("PartyMemberID"))){
						if(jsonObject3.getBoolean("IsFinished")){
							partyMembers.set("Note", "已完成");
						}else{
							partyMembers.set("Note", "未完成");
						}
					}
				}
			}
			for (PartyMembers partyMembers : list_member_noAssessed) {
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
//				partyMembers.set("Note","未完成");
//				partyMembers.remove("Note");
				partyMembers.remove("IsSecretary");
				partyMembers.remove("DisplayOrder");
				partyMembers.remove("PartyGroup");
			}
			
			// 将个人信息表单中除去完成的信息，发送给前端
			jsonObject.put("status", new JsonMsg());
			jsonObject.put("isAssessed", true);
			jsonObject.put("info_record", jsonArray);
			jsonObject.put("list_member_Assessed", list_member_noAssessed);//之前提交的记录集合
			jsonObject.put("info_question", list_question);
			jsonObject.put("info_redLine", list_redLine);
			
			
			
			
			
			jsonObject.put("info_C_determine", partyBranch_VideoInfo.getStr("C_determine"));//请您依据党员实情，按照画面评议内容，逐一对您所在支部的其他党员予以赋分，“党员互评”开始，请选择你要评议的党员
			jsonObject.put("info_D_member", partyBranch_VideoInfo.getStr("D_member"));//请您在赋分完成后，点击“提交”按键
			
			jsonObject.put("info_D_question_assessing", partyBranch_VideoInfo.getStr("D_question_assessing"));//请完成没有作答的问题！
			jsonObject.put("info_D_question_assessed", partyBranch_VideoInfo.getStr("D_question_assessed"));//请进行红线问题作答
			
			jsonObject.put("info_D_redLine_assessing", partyBranch_VideoInfo.getStr("D_redLine_assessing"));//请完成没有作答的红线问题！
			jsonObject.put("info_D_redLine_assessed", partyBranch_VideoInfo.getStr("D_redLine_assessed"));//已完成评价，请点击“完成”按键完成评价过程。
			
			jsonObject.put("info_D_redLine_determine", partyBranch_VideoInfo.getStr("D_redLine_determine"));//请选择下一位您要评价的党员
			jsonObject.put("info_D_all_assessed", partyBranch_VideoInfo.getStr("D_all_assessed"));//所有评价已完成，请点击“提交”按键，提交评价数据。
			
			jsonObject.put("info_D_all_determine", partyBranch_VideoInfo.getStr("D_all_determine"));//null
			
			System.out.println("-------------json--------------:"+jsonObject);
			
		}

		return jsonObject;
	}

	/**
	 * 
	 * <p>
	 * 获得非红线问题集合
	 * 
	 * @return list 非红线问题集合
	 */
	private List<Q_OrgAssess_Question> questionCollection() {
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[Q_OrgAssess_Question] where IsUse = '1' order by DisplayOder";
		List<Q_OrgAssess_Question> list = Q_OrgAssess_Question.dao.find(sql);
		return list;

	}

	/**
	 * 
	 * <p>
	 * 获得红线问题集合
	 * 
	 * @return list 红线问题集合
	 */
	private List<RedLine> redLineCollection() {
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[RedLine] order by RedLineID";
		List<RedLine> list = RedLine.dao.find(sql);
		return list;

	}
	/*
	  { "info_member": [ { "PartyMemberID": "001", "IsFinished": 1, "Answer_0":
	  "9", "Answer_1": "8", "Answer_2": "9", "Answer_3": "9", "Answer_4": "9",
	  "Answer_5": "8", "Answer_6": "9", "Answer_7": "9", "Answer_8": "9",
	  "Answer_9": "9", "A": "无", "B": "无", "C": "无", "D": "无", "E": "无", "F":
	  "无", "G": "无", "H": "无", "I": "无", "J": "无" },*/
	 

	/**
	 * <p>
	 * 保存已经提交的答案<br>
	 * 把前端传来的Json解析并将成绩简单计算，将计算结果和个人信息存入数据库
	 * 
	 * @param s
	 *            提交的答案
	 * @return jsonMsg 状态集合
	 */
    //组织提交哪几个用户信息，就给后台发送相关的信息，不要发送无关的其他信息
	public JsonMsg save_questionAndAnswer(JSONObject s) {
		JsonMsg jsonMsg = null;// 用于返回状态；；
		JSONObject json_member = new JSONObject(true);
		JSONArray json_members = s.getJSONArray("info_member");// 获取答案集合
		System.out.println("json_members:" + json_members);
		for (int i = 0; i < json_members.size(); i++) {

			json_member = json_members.getJSONObject(i);

			assessedId_static = (String) json_member.get("PartyMemberID");
			String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyMembers] WHERE ParentID = ? AND PartyMemberID = ? AND PartyBranchID = ?";
			PartyMembers partyMember = PartyMembers.dao.findFirst(sql, parentPartyId_static, assessedId_static,
					partyBranchId__static);
			// 转化时间格式：
			Date date = (partyMember.get("Birthday"));
			String str_bir = DateUtils.dateToStr(date);

			// 计算互评结果，存储到sum_zzp中;(需要将sum_zzp和红线问题的答案更新到PartySpiritCheck_SumAssesment表中)
			int sum_zzp = 0, // zz评的分数
					num_ques = questionCollection().size();// 问题的数量
			for (int j = 0; j < num_ques; j++) {
				String answer = "Answer_" + j;
				// String question = "Question_"+i;
				try {
					sum_zzp = Integer.parseInt(
							(json_member.getString(answer).equals("")) ? "0" : json_member.getString(answer)) + sum_zzp;
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				// json.put(question, list.get())

			}

			// 红线问题答案，放入list中
			List<String> list = new ArrayList<>();
			if (json_member.getString("A").equals("有"))
				list.add("A");
			if (json_member.getString("B").equals("有"))
				list.add("B");
			if (json_member.getString("C").equals("有"))
				list.add("C");
			if (json_member.getString("D").equals("有"))
				list.add("D");
			if (json_member.getString("E").equals("有"))
				list.add("E");
			if (json_member.getString("F").equals("有"))
				list.add("F");
			if (json_member.getString("G").equals("有"))
				list.add("G");
			if (json_member.getString("H").equals("有"))
				list.add("H");
			if (json_member.getString("I").equals("有"))
				list.add("I");
			if (json_member.getString("J").equals("有"))
				list.add("J");

			// 拼接Json串，放入到
			// Q_MassAssess_Answer_Replay表的SelectedChoiceScoreStr列中去
			JSONObject json = new JSONObject(true);
			json = json_member;

			json.put("PartyMemberNo", 0);
			json.put("QuestionNum", num_ques);
			json.put("rowNumber", partyMember.get("rowNumber"));
			json.put("PartyBranchID", partyMember.get("PartyBranchID"));
			json.put("Name", partyMember.get("Name"));
			json.put("Sex", partyMember.get("Sex"));
			json.put("Birthday", str_bir);
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
			json.put("ScoreSum", sum_zzp);

			// 计算totalScore
//			String sql_sum = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartySpiritCheck_SumAssesment] WHERE ParentID = ? AND PartyBranchID = ? AND Activity_ID = ? AND PartyMemberID_Assessed = ?";
//			PartySpiritCheck_SumAssesment SumAssesment = PartySpiritCheck_SumAssesment.dao.findFirst(sql_sum,
//					parentPartyId_static, partyBranchId__static, activityId_static, assessedId_static);
//
//			double totalScore = SumAssesment.getFloat("SelfAssessmentScore") * 0.2
//					+ SumAssesment.getFloat("MutualAssessScore") * 0.25
//					+ SumAssesment.getFloat("MassAssessScore") * 0.25 + sum_zzp * 0.3;
//			System.out.println("totalScore:" + dataFormat.doubleToTwo(totalScore));

			// 存放红线问题
			String ss = "";
			// 有红线问题
			if (list.size() > 0)
				for (String string : list) {
					ss = ss + string + ",";
				}
			// 用于判断Q_OrgAssess_Answer_Replay中是否存在用户之前评价的记录，如果有，则只需要更相应的值，否则，则需要插入新的一条数据记录
			String sql_Replay = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[Q_OrgAssess_Answer_Replay] WHERE ParentID = ? AND PartyBranchID = ? AND Activity_ID = ? AND PartyMemberID_Assessed = ?";
			Q_OrgAssess_Answer_Replay Answer_Replay = Q_OrgAssess_Answer_Replay.dao.findFirst(sql_Replay,
					parentPartyId_static, partyBranchId__static, activityId_static, assessedId_static);

			// 事务操作
			float average = sum_zzp;
			String ss_ = ss;
			System.out.println("ss:" + ss);
			JSONObject jsonObject = json;
			// 需要更新，而不是插入
			// 表Q_OrgAssess_Answer_Replay中已存在上次的未完成的数据，所以，这次只能更新Q_OrgAssess_Answer_Replay表中的数据
			if (Answer_Replay != null) {//需要更新操作
			/*	// 事务操作
				boolean succeed = Db.tx(new IAtom() {
					public boolean run() throws SQLException {
						int count = 1, count2 = 0,flag = 0;
						if (jsonObject.getBoolean("IsFinished").equals(1)) {//已经完成评价，所以可以进行PartySpiritCheck_SumAssesment中的更新
							flag = 1;
							// 更新成绩表
							if (list.size() > 0) {// 更新红线问题
								String sql_sum = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartySpiritCheck_SumAssesment]  WHERE ParentID = ? AND PartyBranchID = ? AND Activity_ID = ? AND PartyMemberID_Assessed = ?";
								PartySpiritCheck_SumAssesment q = PartySpiritCheck_SumAssesment.dao.findFirst(sql_sum,
										parentPartyId_static, partyBranchId__static, activityId_static,
										assessedId_static);// 可以细化
								System.out.println("q:---------------------" + q);
								String a, // RedLine_Org
										b;// RedLine_Sum
											// 解决数据库中自带的字符串是'NULL'情况
								if (q.getStr("RedLine_Org") == null) {
									a = ss_;
								} else {
									a = q.getStr("RedLine_Org") + "," + ss_;
								}
								System.out.println("a:" + a);
								if (q.getStr("RedLine_Sum") == null) {
									b = ss_;
								} else {
									b = q.getStr("RedLine_Sum") + "," + ss_;
								}

								System.out.println("b:" + b);

								count = Db.update(
										"update [DB_PartySpritCheck_Test].[dbo].[PartySpiritCheck_SumAssesment] SET TotalScore = ?,OrgAssessScore = ?,RedLine_Org = ?,RedLine_Sum = ? WHERE  ParentID = ? AND PartyBranchID = ? AND Activity_ID = ? AND PartyMemberID_Assessed = ?",
										totalScore, average, a, b, parentPartyId_static, partyBranchId__static,
										activityId_static, assessedId_static);							
								count2 = Db.update(
										"update  [DB_PartySpritCheck_Test].[dbo].[Q_OrgAssess_Answer_Replay] SET AssessingPW = ?,SelectedChoiceScoreStr = ?,SumScore = ?,RedLine = ?,SubmitTime = ?,IsFinish = ? WHERE ParentID = ? AND PartyMemberID_Assessed = ? AND PartyBranchID = ? AND Activity_ID = ?",
										assessingPass_static.toString(), jsonObject.toString(), average, a,
										DateUtils.getNowDateTimeStr(), jsonObject.getBoolean("IsFinished"),
										parentPartyId_static, assessedId_static, partyBranchId__static,
										activityId_static);
							} else {
								count = Db.update(
										"update [DB_PartySpritCheck_Test].[dbo].[PartySpiritCheck_SumAssesment] SET OrgAssessScore = ?,TotalScore = ?  WHERE ParentID = ? AND PartyBranchID = ? AND Activity_ID = ? AND PartyMemberID_Assessed = ?",
										average, totalScore, parentPartyId_static, partyBranchId__static,
										activityId_static, assessedId_static);

								count2 = Db.update(
										"update  [DB_PartySpritCheck_Test].[dbo].[Q_OrgAssess_Answer_Replay] SET AssessingPW = ?,SelectedChoiceScoreStr = ?,SumScore = ?,SubmitTime = ?,IsFinish = ? WHERE ParentID = ? AND PartyMemberID_Assessed = ? AND PartyBranchID = ? AND Activity_ID = ?",
										assessingPass_static.toString(), jsonObject.toString(), average,
										DateUtils.getNowDateTimeStr(), jsonObject.getBoolean("IsFinished"),
										parentPartyId_static, assessedId_static, partyBranchId__static,
										activityId_static);
							}
						} else {
							// 更新成绩表
							if (list.size() > 0) {// 更新红线问题
								count2 = Db.update(
										"update  [DB_PartySpritCheck_Test].[dbo].[Q_OrgAssess_Answer_Replay] SET AssessingPW = ?,SelectedChoiceScoreStr = ?,SumScore = ?,RedLine = ?,SubmitTime = ?,IsFinish = ? WHERE ParentID = ? AND PartyMemberID_Assessed = ? AND PartyBranchID = ? AND Activity_ID = ?",
										assessingPass_static.toString(), jsonObject.toString(), average, ss_,
										DateUtils.getNowDateTimeStr(), jsonObject.getBoolean("IsFinished"),
										parentPartyId_static, assessedId_static, partyBranchId__static,
										activityId_static);
							} else {
								count2 = Db.update(
										"update  [DB_PartySpritCheck_Test].[dbo].[Q_OrgAssess_Answer_Replay] SET AssessingPW = ?,SelectedChoiceScoreStr = ?,SumScore = ?,SubmitTime = ?,IsFinish = ? WHERE ParentID = ? AND PartyMemberID_Assessed = ? AND PartyBranchID = ? AND Activity_ID = ?",
										assessingPass_static.toString(), jsonObject.toString(), average,
										DateUtils.getNowDateTimeStr(), jsonObject.getBoolean("IsFinished"),
										parentPartyId_static, assessedId_static, partyBranchId__static,
										activityId_static);
							}
						}

						System.out.println("count:" + count);
						System.out.println("count2:" + count2);
						if (flag == 1) {
							return count2 == 1 && count == 1;
						}
						return count2 == 1 ;
					}
				});

				if (!succeed)
					return (new JsonMsg(Code.C_107));
			}*/
				int  count2 = 0;
				if (list.size() > 0) {// 更新红线问题
					String sql_sum = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartySpiritCheck_SumAssesment]  WHERE ParentID = ? AND PartyBranchID = ? AND Activity_ID = ? AND PartyMemberID_Assessed = ?";
					PartySpiritCheck_SumAssesment q = PartySpiritCheck_SumAssesment.dao.findFirst(sql_sum,
							parentPartyId_static, partyBranchId__static, activityId_static,
							assessedId_static);// 可以细化
					System.out.println("q:---------------------" + q);
					String a, // RedLine_Org
							b;// RedLine_Sum
								// 解决数据库中自带的字符串是'NULL'情况
					if (q.getStr("RedLine_Org") == null) {
						a = ss_;
					} else {
						a = q.getStr("RedLine_Org") + "," + ss_;
					}
					System.out.println("a:" + a);
					if (q.getStr("RedLine_Sum") == null) {
						b = ss_;
					} else {
						b = q.getStr("RedLine_Sum") + "," + ss_;
					}
					count2 = Db.update(
							"update  [DB_PartySpritCheck_Test].[dbo].[Q_OrgAssess_Answer_Replay] SET AssessingPW = ?,SelectedChoiceScoreStr = ?,SumScore = ?,RedLine = ?,SubmitTime = ?,IsFinish = ? WHERE ParentID = ? AND PartyMemberID_Assessed = ? AND PartyBranchID = ? AND Activity_ID = ?",
							assessingPass_static.toString(), jsonObject.toString(), average, a,
							DateUtils.getNowDateTimeStr(), jsonObject.getBoolean("IsFinished"),
							parentPartyId_static, assessedId_static, partyBranchId__static,
							activityId_static);
				}else {
					count2 = Db.update(
							"update  [DB_PartySpritCheck_Test].[dbo].[Q_OrgAssess_Answer_Replay] SET AssessingPW = ?,SelectedChoiceScoreStr = ?,SumScore = ?,SubmitTime = ?,IsFinish = ? WHERE ParentID = ? AND PartyMemberID_Assessed = ? AND PartyBranchID = ? AND Activity_ID = ?",
							assessingPass_static.toString(), jsonObject.toString(), average,
							DateUtils.getNowDateTimeStr(), jsonObject.getBoolean("IsFinished"),
							parentPartyId_static, assessedId_static, partyBranchId__static,
							activityId_static);
				}
				if (count2!=1)
					return (new JsonMsg(Code.C_107));
				
			}
			else {// 用户之前未留下记录，直接将数据插入到数据库
					// 用于插入到表中
				Record q_OrgAssess_Answer_Replay = new Record();
				q_OrgAssess_Answer_Replay.set("Activity_ID", activityId_static.toString());
				q_OrgAssess_Answer_Replay.set("PartyBranchID", partyBranchId__static.toString());
				q_OrgAssess_Answer_Replay.set("PartyMemberID_Assessed", assessedId_static.toString());
				q_OrgAssess_Answer_Replay.set("AssessingPW", assessingPass_static.toString());
				q_OrgAssess_Answer_Replay.set("SelectedChoiceScoreStr", jsonObject.toString());
				q_OrgAssess_Answer_Replay.set("SumScore", sum_zzp);
				// 有红线问题
				if (list.size() > 0)
					q_OrgAssess_Answer_Replay.set("RedLine", ss_.toString());
				q_OrgAssess_Answer_Replay.set("SubmitTime", DateUtils.getNowDateTimeStr());
				q_OrgAssess_Answer_Replay.set("IsFinish", jsonObject.getBoolean("IsFinished"));
				q_OrgAssess_Answer_Replay.set("ParentID", parentPartyId_static.toString());
				// 事务操作
//				System.out.println("ss_:" + ss);
//				boolean succeed = Db.tx(new IAtom() {
//					public boolean run() throws SQLException {
//						int count = 1;
//						if (jsonObject.getBoolean("IsFinished")) {
//							// 更新成绩表(如果是完成的记录，则直接更新总成绩表，否则不更新。但是无论怎样，都要插入到记录表单Q_OrgAssess_Answer_Replay中)
//							if (list.size() > 0) {// 更新红线问题
//								String sql_sum = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartySpiritCheck_SumAssesment]  WHERE ParentID = ? AND PartyBranchID = ? AND Activity_ID = ? AND PartyMemberID_Assessed = ?";
//								PartySpiritCheck_SumAssesment q = PartySpiritCheck_SumAssesment.dao.findFirst(sql_sum,
//										parentPartyId_static, partyBranchId__static, activityId_static,
//										assessedId_static);// 可以细化
//								System.out.println("q:---------------------" + q);
//								String a, // RedLine_Org
//										b;// RedLine_Sum
//											// 解决数据库中自带的字符串是'NULL'情况
//								if (q.getStr("RedLine_Org") == null) {
//									a = ss_;
//								} else {
//									a = q.getStr("RedLine_Org") + "," + ss_;
//								}
//								System.out.println("a:" + a);
//								if (q.getStr("RedLine_Sum") == null) {
//									b = ss_;
//								} else {
//									b = q.getStr("RedLine_Sum") + "," + ss_;
//								}
//
//								System.out.println("b:" + b);
//
//								count = Db.update(
//										"update [DB_PartySpritCheck_Test].[dbo].[PartySpiritCheck_SumAssesment] SET TotalScore = ?,OrgAssessScore = ?,RedLine_Org = ?,RedLine_Sum = ? WHERE  ParentID = ? AND PartyBranchID = ? AND Activity_ID = ? AND PartyMemberID_Assessed = ?",
//										totalScore, average, a, b, parentPartyId_static, partyBranchId__static,
//										activityId_static, assessedId_static);
//							} else {
//								count = Db.update(
//										"update [DB_PartySpritCheck_Test].[dbo].[PartySpiritCheck_SumAssesment] SET OrgAssessScore = ?,TotalScore = ?  WHERE ParentID = ? AND PartyBranchID = ? AND Activity_ID = ? AND PartyMemberID_Assessed = ?",
//										average, totalScore, parentPartyId_static, partyBranchId__static,
//										activityId_static, assessedId_static);
//							}
//						}
//						// 保存到答案表
//						boolean count2 = Db.save("[DB_PartySpritCheck_Test].[dbo].[Q_OrgAssess_Answer_Replay]",
//								q_OrgAssess_Answer_Replay);
//						System.out.println("count:" + count);
//						System.out.println("count2:" + count2);
//						return count2 && count == 1;
//					}
//				});
				
				// 保存到答案表
				boolean count2 = Db.save("[DB_PartySpritCheck_Test].[dbo].[Q_OrgAssess_Answer_Replay]",
						q_OrgAssess_Answer_Replay);
				if (!count2)
					return (new JsonMsg(Code.C_107));
			}
		}
		// 更新用户表
		Db.update(
				"update [DB_PartySpritCheck_Test].[dbo].[PartyBranch] SET IsOrganizeAssess = ? WHERE ParentID = ?  AND PartyBranchID = ?",
				1, parentPartyId_static, partyBranchId__static);
		jsonMsg = new JsonMsg();
		return jsonMsg;

	}

	public File getExcel() {

		int count = 0;
		String title = DateUtils.getNowDateTimeStr1();
		System.out.println(title);
		File file = new File(title + "_统计表" + ".xls");

//		String sql = "select ParentID,PartyBranchID,Activity_ID,SelfAssessmentScore,MutualAssessScore,RedLine_Mutual,MassAssessScore,RedLine_Mass,OrgAssessScore,RedLine_Org,TotalScore,RedLine_Sum,PartySpritJudge,ExistingProblems,ImprovingSuggestions from PartySpiritCheck_SumAssesment where ParentID = '029-0001' And PartyBranchID = '01' And Activity_ID = '001'";
		String sql = "select ParentID,PartyBranchID,Activity_ID,SelfAssessmentScore,MutualAssessScore,RedLine_Mutual,MassAssessScore,RedLine_Mass,OrgAssessScore,RedLine_Org,TotalScore,RedLine_Sum,PartySpritJudge,ExistingProblems,ImprovingSuggestions from PartySpiritCheck_SumAssesment where ParentID = ? And PartyBranchID = ? And Activity_ID = ?";
		System.out.println("sql:" + sql);
		// ider:外部传入一个表头、SQL语句、
		String s[] = new String[] { "ParentID", "PartyBranchID", "Activity_ID", "SelfAssessmentScore",
				"MutualAssessScore", "RedLine_Mutual", "MassAssessScore", "RedLine_Mass", "OrgAssessScore",
				"RedLine_Org", "TotalScore", "RedLine_Sum", "PartySpritJudge", "ExistingProblems",
				"ImprovingSuggestions" };

		// 创建工作薄
		HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
		// sheet:一张表的简称
		// row:表里的行
		// 创建工作薄中的工作表
		HSSFSheet hssfSheet = hssfWorkbook.createSheet("test");
		// 创建行
		HSSFRow row = hssfSheet.createRow(0);
		// 创建单元格，设置表头 创建列
		HSSFCell cell = null;
		// 便利表头
		for (int i = 0; i < s.length; i++) {
			// 创建传入进来的表头的个数
			cell = row.createCell(i);
			// 表头的值就是传入进来的值
			cell.setCellValue(s[i]);

		}
		// 新增一个行就累加
		row = hssfSheet.createRow(++count);

		// 得到所有记录 行：列
		List<PartySpiritCheck_SumAssesment> list = PartySpiritCheck_SumAssesment.dao.find(sql, parentPartyId_static,partyBranchId__static,activityId_static);
		System.out.println("list:" + list);
		PartySpiritCheck_SumAssesment record = null;

		if (list != null) {
			// 获取所有的记录 有多少条记录就创建多少行
			for (int i = 0; i < list.size(); i++) {
				System.out.println(i);
				row = hssfSheet.createRow(++count);
				// 得到所有的行 一个record就代表 一行
				record = list.get(i);
				// 在有所有的记录基础之上，便利传入进来的表头,再创建N行
				for (int j = 0; j < s.length; j++) {
					cell = row.createCell(j);
					// 把每一行的记录再次添加到表头下面 如果为空就为 "" 否则就为值
					cell.setCellValue(record.get(s[j]) == null ? "" : record.get(s[j]).toString());
				}
			}
		}
		try {
			FileOutputStream fileOutputStreane = new FileOutputStream(file);
			hssfWorkbook.write(fileOutputStreane);
			fileOutputStreane.flush();
			fileOutputStreane.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (file.isFile()) {
			System.out.println("11111");
			return file;
		} else {
			return null;
		}
	}
	
	 public JSONObject video_words(JSONObject s) throws Exception{
		 return TtsMain.tts1(parentPartyId_static, "党性组织评端", s.getString("words"));
	 }
	
}
