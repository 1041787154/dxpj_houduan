package com.dxpj.hp.server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dxpj.hp.model.Q_MutualAssess_Answer_Replay;
import com.dxpj.hp.model.Q_MutualAssess_Question;
import com.dxpj.hp.model.RedLine;
import com.dxpj.model.backGround;
import com.dxpj.util.Code;
import com.dxpj.util.DataFormat;
import com.dxpj.util.DateUtils;
import com.dxpj.util.JsonMsg;
import com.dxpj.util.TtsMain;
import com.dxpj.zp.model.PartyBranch;
import com.dxpj.zp.model.PartyBranch_VideoInfo;
import com.dxpj.zp.model.PartyMembers;
import com.dxpj.zp.model.PartySpiritCheck_ActivityInfo;
import com.dxpj.zzp.model.Q_OrgAssess_Answer_Replay;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class HPserver {

	// 存在的问题：未将正在评价的用户的状态改为1
	//保存问题时，前端是全部都返回还是只返回已经评价完成的
	/**
	 * 活动信息对象<br>
	 * 其中上级党组织id目前只能是029-0001
	 * 
	 */
	PartySpiritCheck_ActivityInfo p_ActivityInfo;
	DataFormat dataFormat = new DataFormat();
	private static PartyBranch_VideoInfo partyBranch_VideoInfo;
	
	private static String parentPartyId_static, // 上级组织id
			partyMemberId_static, // 党支部内部的党员id
			partyMemberName_static, // 党支部内部的党员名字
			partyBranchId__static, // 党支部id
			activityId_static, // 活动id
			assessedId_static,
			partyMemberPass_static;

	
	//AssesingTypeName     A_description
	
	
	
	
		//党性自评端
		//渭阳西路街道党员党性教育体检中心
			public JSONObject get_A_Description(String id_parentParty) {
				parentPartyId_static = id_parentParty;
				JSONObject jsonObject = new JSONObject(true);
				String sql= "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyBranch_VideoInfo] WHERE AssesingTypeID = '02' AND ParentID = ?";
				partyBranch_VideoInfo = PartyBranch_VideoInfo.dao.findFirst(sql, parentPartyId_static);
				if (partyBranch_VideoInfo!=null) {
					jsonObject.put("status", new JsonMsg());
					jsonObject.put("info_AssesingTypeName", partyBranch_VideoInfo.getStr("AssesingTypeName"));//党性互评端
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
	 * 返回具有党性评价资格的党支部集合
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
			jsonObject.put("info_A_input", partyBranch_VideoInfo.getStr("A_input"));//欢迎您来到党性体检第二环节 党员互评，
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
	 * 获得党员集合<br>
	 * 
	 * 通过党支部id获得有资格进行党性自评的党员集合
	 * 
	 * 上级党组织id
	 * 
	 * @param id_partyBranch
	 *            党支部id
	 * @return jsonObject 党员集合
	 */
	public JSONObject get_partyMember_byPartyId(String id_partyBranch) {
		partyBranchId__static = id_partyBranch;
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyMembers] WHERE PartyBranchID = ? AND ParentID = ? AND IsParticipatePartySpiritCheck = '1' order by rowNumber";
		List<PartyMembers> list = PartyMembers.dao.find(sql, partyBranchId__static, parentPartyId_static);
		System.out.println("list:" + list);
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
			jsonObject.put("info_C_pass", partyBranch_VideoInfo.getStr("C_pass"));//请输入登陆密码
			return jsonObject;
		} else {
			jsonObject.put("status", new JsonMsg(Code.C_104));
			return jsonObject;
		}
	}

	/**
	 * <p>
	 * 检验登录用户的信息<br>
	 *
	 * 根据上级党组织id、党支部内部的党员id、党支部id、 党员密码进行登录验证，<br>
	 * 其中上级党组织id目前只能是029-0001
	 * 
	 * @param id_partyMember
	 *            党支部内部的党员id
	 * @param pass
	 *            党员密码
	 * 
	 * @return jsonObject 验证结果
	 */
	public JSONObject judge_partyMember_byMemberId(String id_partyMember, String pass) {
		partyMemberPass_static = pass;
		partyMemberId_static = id_partyMember;
		JSONObject jsonObject = new JSONObject(true);
		
		
		
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyMembers] WHERE ParentID =? AND PartyMemberID = ? AND PartyBranchID = ? AND PW_PartySpiritCheck_SelfAssess = ?";
		PartyMembers partyMember = PartyMembers.dao.findFirst(sql, parentPartyId_static, partyMemberId_static,
				partyBranchId__static, partyMemberPass_static);
		
		
		
		

		if (partyMember != null) {
			String sql_member = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyMembers] WHERE ParentID =? AND PartyBranchID = ? AND PartyGroup = ?";
			List<PartyMembers> list_member = PartyMembers.dao.find(sql_member, parentPartyId_static,
					partyBranchId__static, partyMember.getStr("PartyGroup"));// 可以细化
			partyMemberName_static = partyMember.getStr("Name");
			// 已经评价完成
			if (partyMember.getInt("IsMutualAssess") == 2) {
				jsonObject.put("status", new JsonMsg(Code.C_105));// 您已经完成提交，无需再次登录
				
				
				
				
				
				
//				jsonObject.put("status", new JsonMsg(Code.C_105));
				// 没有评价完成
			} else if (partyMember.getInt("IsMutualAssess") == 0) {
				
				if (list_member != null && list_member.size() > 1) {// 存在互评对象
					

					// 从记录表中提交完整信息的记录个数
					/*String sql_member1 = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[Q_MutualAssess_Answer_Replay] WHERE ParentID =? AND PartyBranchID = ? AND Activity_ID = ? AND PartyMemberID_Assessing = ? AND IsFinish = '1'";// 默认取出党支部内的所有党员
					List<Q_MutualAssess_Answer_Replay> list_member_beWrited = Q_MutualAssess_Answer_Replay.dao.find(sql_member1,
							parentPartyId_static, partyBranchId__static, activityId_static,partyMemberId_static);// 可以细化
					if (list_member_beWrited != null) {
						if (list_member_beWrited.size() <(int) Math.ceil(list_member.size()*0.8)) {
							jsonObject.put("status", new JsonMsg());
							
						} else {
							jsonObject.put("status", new JsonMsg(Code.C_105));// 您已经完成提交，无需再次登录
						}
					}*/
					
					
					
					
					jsonObject.put("status", new JsonMsg());
				} else {
					jsonObject.put("status", new JsonMsg(Code.C_108));
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
	 * 根据上级党组织id、党支部内部的党员id、党支部id、 党员密码进行用户获取，<br>
	 * 其中上级党组织id目前只能是029-0001
	 * 
	 * @return jsonObject 信息集合（包含问题、个人信息等）
	 */
	public JSONObject get_questionAndAnswer() {
		JSONObject jsonObject = new JSONObject(true);
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyMembers] WHERE ParentID =? AND PartyMemberID = ? AND PartyBranchID = ? AND PW_PartySpiritCheck_SelfAssess = ?";
		PartyMembers partyMember = PartyMembers.dao.findFirst(sql, parentPartyId_static, partyMemberId_static,
				partyBranchId__static, partyMemberPass_static);
		partyMemberName_static = partyMember.getStr("Name");
		String sql_member = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyMembers] WHERE ParentID = ? AND PartyBranchID = ? AND PartyGroup = ? AND IsParticipatePartySpiritCheck = '1' order by rowNumber";
		List<PartyMembers> list_member = PartyMembers.dao.find(sql_member, parentPartyId_static, partyBranchId__static,
				partyMember.getStr("PartyGroup"));// 可以细化
		list_member.remove(partyMember);
		for (PartyMembers partyMembers : list_member) {
			partyMembers.set("Birthday", DateUtils.simDateToStr(partyMembers.get("Birthday")));
			partyMembers.remove("rowNumber");
			partyMembers.remove("ParentID");
			partyMembers.remove("PartyBranchID");
			partyMembers.remove("PartyBranchName");	
			partyMembers.remove("PartyMemberLableName");
			partyMembers.remove("JoinPartyTime");
			partyMembers.remove("WorkUnit");
			partyMembers.remove("PartyPosition");
			partyMembers.remove("PartyMemberLableID");
			partyMembers.remove("IsParticipatePartySpiritCheck");
			partyMembers.remove("PW_PartySpiritCheck_SelfAssess");
			partyMembers.remove("IsSelfAssessment");
			partyMembers.remove("PW_PartySpiritCheck_MutualAssess");
			partyMembers.remove("IsMutualAssess");
//			partyMembers.set("IsValid","未完成");
//			partyMembers.set("IsMutualAssess","未完成");
			partyMembers.remove("PW_Login");
			partyMembers.remove("IsValid");
//			partyMembers.remove("Note");
			partyMembers.set("Note","未完成");
			partyMembers.remove("IsSecretary");
			partyMembers.remove("DisplayOrder");
			partyMembers.remove("PartyGroup");
		}
		// 成绩表单
		String sql_member_record = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[Q_MutualAssess_Answer_Replay] WHERE ParentID =? AND PartyBranchID = ? AND Activity_ID = ? AND PartyMemberID_Assessing = ?";// 记录信息
		List<Q_MutualAssess_Answer_Replay> list_member_assessed = Q_MutualAssess_Answer_Replay.dao.find(sql_member_record,
							parentPartyId_static, partyBranchId__static, activityId_static,partyMemberId_static);// 可以细化
		
		JSONObject jsonObject3 = new JSONObject();
		for (PartyMembers partyMembers : list_member) {//成员
//			partyMembers.set("Note", "未完成");
			for (int j = 0; j < list_member_assessed.size(); j++) {
				String str = list_member_assessed.get(j).getStr("SelectedChoiceScoreStr").toString();
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
		
		
		partyMember.remove("rowNumber");
		partyMember.remove("ParentID");
		partyMember.remove("PartyBranchID");
		partyMember.remove("Sex");	
		partyMember.remove("Birthday");
		partyMember.remove("PartyMemberLableName");
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
		List<Q_MutualAssess_Question> list_question = questionCollection();
		for (Q_MutualAssess_Question q_MutualAssess_Question : list_question) {
			q_MutualAssess_Question.remove("IsUse");
		}
		List<RedLine> list_redLine = redLineCollection();
		jsonObject.put("status", new JsonMsg());
		jsonObject.put("info_member_assessing", partyMember);
		jsonObject.put("info_member_assessed", list_member);
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
		
		return jsonObject;
	}

	/**
	 * 
	 * <p>
	 * 获得非红线问题集合
	 * 
	 * @return list 非红线问题集合
	 */
	private List<Q_MutualAssess_Question> questionCollection() {
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[Q_MutualAssess_Question] where IsUse = '1' order by DisplayOder";
		List<Q_MutualAssess_Question> list = Q_MutualAssess_Question.dao.find(sql);
		
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
		JsonMsg jsonMsg = new JsonMsg();// 用于返回状态；；
		JSONObject json_member = new JSONObject(true);// 答案---一个对象的
		JSONArray json_members = s.getJSONArray("info_member");// 获取答案集合
		int num_finished = 0;
		for (int i = 0; i < json_members.size(); i++) {
			if (json_members.getJSONObject(i).get("IsFinished").equals(1)) {
				++num_finished;
			}
		}
		if (num_finished < (int) Math.ceil(json_members.size() * 0.8)) {// 最少完成80的任务量
			jsonMsg = new JsonMsg(Code.C_109);
		} else {
			for (int i = 0; i < json_members.size(); i++) {
				json_member = json_members.getJSONObject(i);

				 if (json_member.getBoolean("IsFinished")) {
				//前端只返回已经完成的或者全部都返回
				assessedId_static = json_member.getString("PartyMemberID");
				String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyMembers] WHERE ParentID = ? AND PartyMemberID = ? AND PartyBranchID = ?";
				PartyMembers partyMember = PartyMembers.dao.findFirst(sql, parentPartyId_static, assessedId_static,
						partyBranchId__static);
				// 转化时间格式：
				Date date = partyMember.get("Birthday");
				String str_bir = DateUtils.dateToStr(date);

				// 计算互评结果，存储到sum_hp中;(需要将sum和红线问题的答案更新到PartySpiritCheck_SumAssesment表中)
				int sum_hp = 0, // 互评的分数
						num_ques = questionCollection().size();// 问题的数量
				for (int j = 0; j < num_ques; j++) {
					String answer = "Answer_" + j;
					// String question = "Question_"+i;
					try {
						sum_hp = Integer.parseInt(
								(json_member.getString(answer).equals("")) ? "0" : json_member.getString(answer))
								+ sum_hp;
						// sum_hp =
						// Integer.parseInt(json_member.getString(answer)) +
						// sum_hp;
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
					// json.put(question, list.get())

				}
				// 对该党员是否评价完成

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
				// Q_MutualAssess_Answer_Replay表的SelectedChoiceScoreStr列中去
				JSONObject json = new JSONObject(true);
				json = json_member;

				json.put("PartyMemberNo", 0);
				json.put("QuestionNum", num_ques);
				json.put("rowNumber", partyMember.get("rowNumber"));
				json.put("PartyBranchID", partyMember.get("PartyBranchID"));
				// json.put("PartyMemberID",
				// partyMember.get("PartyMemberID"));
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
				json.put("ScoreSum", sum_hp);

				Record q_MutualAssess_Answer_Replay = new Record();// 用于插入到表中

				q_MutualAssess_Answer_Replay.set("Activity_ID", activityId_static);
				q_MutualAssess_Answer_Replay.set("PartyBranchID", partyBranchId__static);
				q_MutualAssess_Answer_Replay.set("PartyMemberID_Assessed", assessedId_static);
				q_MutualAssess_Answer_Replay.set("PartyMemberID_Assessing", partyMemberId_static);
				q_MutualAssess_Answer_Replay.set("AssessingPW", partyMemberPass_static.toString());
				q_MutualAssess_Answer_Replay.set("SelectedChoiceScoreStr", json.toString());
				q_MutualAssess_Answer_Replay.set("SumScore", sum_hp);

				String ss = "";
				// 有红线问题
				if (list.size() > 0)

				{
					for (String string : list) {
						ss = ss + string + ",";
					}
					q_MutualAssess_Answer_Replay.set("RedLine", ss.toString());
				}
				q_MutualAssess_Answer_Replay.set("SubmitTime", DateUtils.getNowDateTimeStr());
				q_MutualAssess_Answer_Replay.set("IsFinish", json.get("IsFinished"));
				q_MutualAssess_Answer_Replay.set("ParentID", parentPartyId_static.toString());
				q_MutualAssess_Answer_Replay.set("PartyMemberName_Assessing", partyMemberName_static.toString());

				System.out.println("Q_MutualAssess_Answer_Replay:" + q_MutualAssess_Answer_Replay);

				// 互评的平均数
//				String sql_members = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[Q_MutualAssess_Answer_Replay] WHERE ParentID =? AND PartyBranchID = ? AND Activity_ID = ? AND PartyMemberID_Assessed = ? AND IsFinish = '1'";
//				List<Q_MutualAssess_Answer_Replay> list_members = Q_MutualAssess_Answer_Replay.dao.find(sql_members,
//						parentPartyId_static, partyBranchId__static, activityId_static, assessedId_static);// 可以细化
//				float average, sum_ave = 0;
//				for (Q_MutualAssess_Answer_Replay q_MutualAssess_Answer_Replay2 : list_members) {
//					sum_ave = q_MutualAssess_Answer_Replay2.getInt("SumScore") + sum_ave;
//				}
//				sum_ave = sum_ave + sum_hp;
//				average = sum_ave / (list_members.size() + 1);// 互评的平均数，需要保存到PartySpiritCheck_SumAssesment表中

				// 计算totalScore
//				String sql_sum = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartySpiritCheck_SumAssesment] WHERE ParentID = ? AND PartyBranchID = ? AND Activity_ID = ? AND PartyMemberID_Assessed = ?";
//				PartySpiritCheck_SumAssesment SumAssesment = PartySpiritCheck_SumAssesment.dao.findFirst(sql_sum,
//						parentPartyId_static, partyBranchId__static, activityId_static, assessedId_static);
//
//				double totalScore = SumAssesment.getFloat("SelfAssessmentScore") * 0.2 + average * 0.25
//						+ SumAssesment.getFloat("MassAssessScore") * 0.25
//						+ SumAssesment.getFloat("OrgAssessScore") * 0.3;
//				System.out.println("totalScore:" + dataFormat.doubleToTwo(totalScore));

				// if (json_member.get("isfinshed").equals(0))
				// boo = false;
				// 事务操作
//				String ss_ = ss;
//				JSONObject jsonObject = json_member;
//				System.out.println("ss_:" + ss_);
//				boolean succeed = Db.tx(new IAtom() {
//					public boolean run() throws SQLException {
//						int 
//						count = 0,
//						flag = 0;
//						if (jsonObject.getBoolean("IsFinished").equals(1)) {
//							flag  = 1;
							// 更新成绩表
//							if (list.size() > 0) {// 更新红线问题
//								// String ss = null;
//								// for (String string : list) {
//								// ss = ss + string + ",";
//								// }
//								String sql_sum = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartySpiritCheck_SumAssesment]  WHERE ParentID = ? AND PartyBranchID = ? AND Activity_ID = ? AND PartyMemberID_Assessed = ?";
//								PartySpiritCheck_SumAssesment q = PartySpiritCheck_SumAssesment.dao.findFirst(sql_sum,
//										parentPartyId_static, partyBranchId__static, activityId_static,
//										assessedId_static);// 可以细化
//								System.out.println("q:---------------------" + q);
//								String a, b;
//								// 解决数据库中自带的字符串是'NULL'情况
//								if (q.getStr("RedLine_Mutual") == null) {
//									a = ss_;
//								} else {
//									a = q.getStr("RedLine_Mutual") + "," + ss_;
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
//										"update [DB_PartySpritCheck_Test].[dbo].[PartySpiritCheck_SumAssesment] SET MutualAssessScore = ?,TotalScore = ? ,RedLine_Mutual = ?,RedLine_Sum = ? WHERE  ParentID = ? AND PartyBranchID = ? AND Activity_ID = ? AND PartyMemberID_Assessed = ?",
//										average, dataFormat.doubleToTwo(totalScore), a, b, parentPartyId_static,
//										partyBranchId__static, activityId_static, assessedId_static);
//							} else {
//								count = Db.update(
//										"update [DB_PartySpritCheck_Test].[dbo].[PartySpiritCheck_SumAssesment] SET MutualAssessScore = ?,TotalScore = ?  WHERE ParentID = ? AND PartyBranchID = ? AND Activity_ID = ? AND PartyMemberID_Assessed = ?",
//										average, dataFormat.doubleToTwo(totalScore), parentPartyId_static,
//										partyBranchId__static, activityId_static, assessedId_static);
//							}
//						}
						
						// 保存到答案表
//						boolean count2 = Db.save("[DB_PartySpritCheck_Test].[dbo].[Q_MutualAssess_Answer_Replay]",
//								q_MutualAssess_Answer_Replay);
//						
//                        return count2 == true;
//					}
//				});、
				boolean count2 = Db.save("[DB_PartySpritCheck_Test].[dbo].[Q_MutualAssess_Answer_Replay]",
						q_MutualAssess_Answer_Replay);
				
//                return count2 == true;

				if (!count2) {
					return (new JsonMsg(Code.C_107));
				}			
			}			
		}
//			int status = 0;
//			String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyMembers] WHERE ParentID =? AND PartyMemberID = ? AND PartyBranchID = ? AND PW_PartySpiritCheck_SelfAssess = ?";
//			PartyMembers partyMember = PartyMembers.dao.findFirst(sql, parentPartyId_static, partyMemberId_static,
//					partyBranchId__static, partyMemberPass_static);
//			
//			
//			
//			String sql_member = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyMembers] WHERE ParentID = ? AND PartyBranchID = ? AND PartyGroup = ? AND IsParticipatePartySpiritCheck = '1' order by rowNumber";
//			List<PartyMembers> list_member = PartyMembers.dao.find(sql_member, parentPartyId_static, partyBranchId__static,
//					partyMember.getStr("PartyGroup"));// 可以细化
//			
//			
//			String sql_member1 = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[Q_MutualAssess_Answer_Replay] WHERE ParentID =? AND PartyBranchID = ? AND Activity_ID = ? AND PartyMemberID_Assessing = ? AND IsFinish = '1'";// 默认取出党支部内的所有党员
//			List<Q_MutualAssess_Answer_Replay> list_member_beWrited = Q_MutualAssess_Answer_Replay.dao.find(sql_member1,
//					parentPartyId_static, partyBranchId__static, activityId_static,partyMemberId_static);// 可以细化
//			if (list_member_beWrited != null) {
//				if (list_member_beWrited.size() <(int) Math.ceil((list_member.size()-1)*0.8)) {
//					status = 0;
//					
//				} else {
//					status = 2;
//				}
//			}
			
//			// 更新用户表
//			Db.update(
//					"update  [DB_PartySpritCheck_Test].[dbo].[PartyMembers] SET IsMutualAssess = ? WHERE ParentID = ? AND PartyMemberID = ? AND PartyBranchID = ?",
//					status, parentPartyId_static, partyMemberId_static, partyBranchId__static);
//			jsonMsg = new JsonMsg();
		}
		System.out.println("jsonMsg:"+jsonMsg);
		return jsonMsg;

	}
	public JsonMsg setAssessingStatus() {
		JsonMsg jsonMsg = new JsonMsg();
		boolean succeed = Db.tx(new IAtom() {
			public boolean run() throws SQLException {
				int bool = Db.update(
						"update  [DB_PartySpritCheck_Test].[dbo].[PartyMembers] SET IsMutualAssess = ? WHERE ParentID = ? AND PartyMemberID = ? AND PartyBranchID = ?",
						2, parentPartyId_static, partyMemberId_static, partyBranchId__static);
				return bool==1;
			}
		});
		
		if (!succeed) {
			jsonMsg = new JsonMsg(Code.C_101);
		}else{
			jsonMsg = new JsonMsg();
		}
		return jsonMsg;
		
		
		
	}
	
	
	 public JSONObject video_words(JSONObject s) throws Exception{
		 return TtsMain.tts1(parentPartyId_static, "党性互评端", s.getString("words"));
	 }

}
