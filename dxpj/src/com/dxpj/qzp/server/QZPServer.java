package com.dxpj.qzp.server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dxpj.hp.model.Q_MutualAssess_Answer_Replay;
import com.dxpj.hp.model.RedLine;
import com.dxpj.qzp.model.MassAssessment_User;
import com.dxpj.qzp.model.Q_MassAssess_Answer_Replay;
import com.dxpj.qzp.model.Q_MassAssess_Question;
import com.dxpj.util.Code;
import com.dxpj.util.DataFormat;
import com.dxpj.util.DateUtils;
import com.dxpj.util.JsonMsg;
import com.dxpj.util.TtsMain;
import com.dxpj.zp.model.PartyBranch;
import com.dxpj.zp.model.PartyBranch_VideoInfo;
import com.dxpj.zp.model.PartyMembers;
import com.dxpj.zp.model.PartySpiritCheck_ActivityInfo;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class QZPServer {

	// 存在的问题：未将正在评价的群众用户的状态改为1
	/**
	 * 活动信息对象<br>
	 * 其中上级党组织id目前只能是029-0001
	 * 
	 */
	PartySpiritCheck_ActivityInfo p_ActivityInfo;
	DataFormat dataFormat = new DataFormat();
	private static PartyBranch_VideoInfo partyBranch_VideoInfo;

	private static String parentPartyId_static, // 上级组织id
			NameAssessing_static, // 党支部内部的党员名字
			partyBranchId__static, // 党支部id
			activityId_static, // 活动id
			assessedId_static, partyMemberPass_static, assessingId_static;

	
	//AssesingTypeName     A_description
			//党性自评端
			//渭阳西路街道党员党性教育体检中心
				public JSONObject get_A_Description(String id_parentParty) {
					parentPartyId_static = id_parentParty;
					JSONObject jsonObject = new JSONObject(true);
					String sql= "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyBranch_VideoInfo] WHERE AssesingTypeID = '03' AND ParentID = ?";
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
			jsonObject.put("info_A_input", partyBranch_VideoInfo.getStr("A_input"));//尊敬的群众代表，您现在所处的位置是渭阳西路街道党员党性教育体检中心，很荣幸邀请您来到这里，对我们的党员进行评议；请选择邀请您前来参加党性评议的支部
			jsonObject.put("info_C_pass", partyBranch_VideoInfo.getStr("C_pass"));//选择完成 请输入登陆密码
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
	 * 检验登录群众的信息<br>
	 *
	 * 根据上级党组织id、党支部id、 群众密码进行登录验证，<br>
	 * 其中上级党组织id目前只能是029-0001
	 * 
	 * @param id_partyBranch
	 *            党支部id
	 * @param pass
	 *            群众密码
	 * 
	 * @return jsonObject 验证结果
	 */
	@SuppressWarnings("unused")
	public JSONObject judge_partyMember_byMemberId(String id_partyBranch, String pass) {
		partyBranchId__static = id_partyBranch;
		partyMemberPass_static = pass;
		JSONObject jsonObject = new JSONObject(true);
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[MassAssessment_User] WHERE ParentID =? AND PartyBranchID = ? AND LoginPW = ?";
		MassAssessment_User massUser = MassAssessment_User.dao.findFirst(sql, parentPartyId_static,
				partyBranchId__static, partyMemberPass_static);
		
		if (massUser != null) {
			NameAssessing_static = massUser.getStr("MassName");
			assessingId_static = massUser.getStr("LoginID");
			// 已经评价完成
			if (massUser.getInt("IsSubmit") == 2) {
				jsonObject.put("status", new JsonMsg(Code.C_105));
				System.out.println("Code.C_105)");
				// 没有评价完成
			} else if (massUser.getInt("IsSubmit") == 0) {
				String sql_member = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyMembers] WHERE ParentID =? AND PartyBranchID = ?";
				List<PartyMembers> list_member = PartyMembers.dao.find(sql_member, parentPartyId_static,
						partyBranchId__static);// 可以细化
				if (list_member != null && list_member.size() > 1) {
					System.out.println("Code.C_100)");
					jsonObject.put("status", new JsonMsg());
				} else {
					System.out.println("Code.C_108)");
					jsonObject.put("status", new JsonMsg(Code.C_108));
					
				}
			}

		} else {
			System.out.println("Code.C_106)");
			jsonObject.put("status", new JsonMsg(Code.C_106));
			
		}
		return jsonObject;
	}

	/**
	 * <p>
	 * 发送问题和相关信息集合<br>
	 *
	 * 根据上级党组织id、党支部id、 群众密码进行登录验证，<br>
	 * 其中上级党组织id目前只能是029-0001
	 * 
	 * @return jsonObject 信息集合（包含问题、个人信息等）
	 */
	public JSONObject get_questionAndAnswer() {
		JSONObject jsonObject = new JSONObject(true);

		String sql_member = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyMembers] WHERE ParentID =? AND PartyBranchID = ? AND IsParticipatePartySpiritCheck = '1' order by rowNumber";
		List<PartyMembers> list_member = PartyMembers.dao.find(sql_member, parentPartyId_static, partyBranchId__static);// 可以细化

		List<Q_MassAssess_Question> list_question = questionCollection();
		for (Q_MassAssess_Question q_MassAssess_Question : list_question) {
			q_MassAssess_Question.remove("IsUse");
		}
		List<RedLine> list_redLine = redLineCollection();

		for (PartyMembers partyMembers : list_member) {
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
//			partyMembers.remove("Note");
			partyMembers.set("Note","未完成");
			partyMembers.remove("IsSecretary");
			partyMembers.remove("DisplayOrder");
			partyMembers.remove("PartyGroup");
		}
		
		// 成绩表单
				String sql_member_record = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[Q_MassAssess_Answer_Replay] WHERE ParentID =? AND PartyBranchID = ? AND Activity_ID = ? AND AssessingPW = ?";// 记录信息
				List<Q_MassAssess_Answer_Replay> list_member_assessed = Q_MassAssess_Answer_Replay.dao.find(sql_member_record,
									parentPartyId_static, partyBranchId__static, activityId_static,partyMemberPass_static);// 可以细化
				
				JSONObject jsonObject3 = new JSONObject();
				for (PartyMembers partyMembers : list_member) {//成员
//					partyMembers.set("Note", "未完成");
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
		
		
		
		
		
		
		
		
		
		jsonObject.put("status", new JsonMsg());
		jsonObject.put("info_member", list_member);
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
			
		// 正在评价中，，，

		return jsonObject;
	}

	/**
	 * 
	 * <p>
	 * 获得非红线问题集合
	 * 
	 * @return list 非红线问题集合
	 */
	private List<Q_MassAssess_Question> questionCollection() {
		String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[Q_MassAssess_Question] where IsUse = '1' order by DisplayOder";
		List<Q_MassAssess_Question> list = Q_MassAssess_Question.dao.find(sql);
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
//	
//	  { "info_member": [ { "PartyMemberID": "001", "IsFinished": 1, "Answer_0":
//	  "9", "Answer_1": "8", "Answer_2": "9", "Answer_3": "9", "Answer_4": "9",
//	  "Answer_5": "8", "Answer_6": "9", "Answer_7": "9", "Answer_8": "9",
//	  "Answer_9": "9", "A": "无", "B": "无", "C": "无", "D": "无", "E": "无", "F":
//	  "无", "G": "无", "H": "无", "I": "无", "J": "无" },
//	 

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
		if (num_finished < (int) Math.ceil(json_members.size() * 0.8)) {
			jsonMsg = new JsonMsg(Code.C_109);
		} else {
			for (int i = 0; i < json_members.size(); i++) {
				json_member = json_members.getJSONObject(i);

				if (json_member.getBoolean("IsFinished")) {
					assessedId_static = json_member.getString("PartyMemberID");
					String sql = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartyMembers] WHERE ParentID = ? AND PartyMemberID = ? AND PartyBranchID = ?";
					PartyMembers partyMember = PartyMembers.dao.findFirst(sql, parentPartyId_static, assessedId_static,
							partyBranchId__static);
					// 转化时间格式：
					Date date = (partyMember.get("Birthday"));
					String str_bir = DateUtils.dateToStr(date);

					// 计算互评结果，存储到sum_qzp中;(需要将sum_qzp和红线问题的答案更新到PartySpiritCheck_SumAssesment表中)
					int sum_qzp = 0, // 群众评的分数
							num_ques = questionCollection().size();// 问题的数量
					for (int j = 0; j < num_ques; j++) {
						String answer = "Answer_" + j;
						// String question = "Question_"+i;
						try {

							sum_qzp = Integer.parseInt(
									(json_member.getString(answer).equals("")) ? "0" : json_member.getString(answer))
									+ sum_qzp;
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
					json.put("QuestionNum",  num_ques);
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
					json.put("ScoreSum", sum_qzp);

					Record q_MassAssess_Answer_Replay = new Record();// 用于插入到表中

					q_MassAssess_Answer_Replay.set("Activity_ID", activityId_static.toString());
					q_MassAssess_Answer_Replay.set("PartyBranchID", partyBranchId__static.toString());
					q_MassAssess_Answer_Replay.set("PartyMemberID_Assessed", assessedId_static.toString());
					q_MassAssess_Answer_Replay.set("MassID_Assessing", assessingId_static.toString());
					q_MassAssess_Answer_Replay.set("AssessingPW", partyMemberPass_static.toString());
					q_MassAssess_Answer_Replay.set("SelectedChoiceScoreStr", json.toString());
					q_MassAssess_Answer_Replay.set("SumScore", sum_qzp);

					String ss = "";
					// 有红线问题
					if (list.size() > 0)

					{
						for (String string : list) {
							ss = ss + string + ",";
						}
						q_MassAssess_Answer_Replay.set("RedLine", ss.toString());
					}
					q_MassAssess_Answer_Replay.set("SubmitTime", DateUtils.getNowDateTimeStr());
					q_MassAssess_Answer_Replay.set("IsFinish", json_member.get("IsFinished"));
					q_MassAssess_Answer_Replay.set("ParentID", parentPartyId_static.toString());
					q_MassAssess_Answer_Replay.set("AssesingMassName", NameAssessing_static.toString());

					System.out.println("q_MassAssess_Answer_Replay:" + q_MassAssess_Answer_Replay);
					System.out.println("parentPartyId_static:" + parentPartyId_static);
					System.out.println("partyBranchId__static:" + partyBranchId__static);
					System.out.println("activityId_static:" + activityId_static);
					System.out.println("assessedId_static:" + assessedId_static);

//					String sql_members = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[Q_MassAssess_Answer_Replay] WHERE ParentID = ? AND PartyBranchID = ? AND Activity_ID = ? AND PartyMemberID_Assessed = ? AND IsFinish = '1'";
//					List<Q_MassAssess_Answer_Replay> list_members = Q_MassAssess_Answer_Replay.dao.find(sql_members,
//							parentPartyId_static, partyBranchId__static, activityId_static, assessedId_static);
//					//
//					//
//					System.out.println("list_members" + list_members);
					
					// 互评的平均数
//					float average, 
//							sum_ave = 0;
//					// ...........average = sum_qzp;
//
//					if (list_members != null) {
//						for (Q_MassAssess_Answer_Replay q_MassAssess_Answer_Replay2 : list_members) {
//							sum_ave = q_MassAssess_Answer_Replay2.getInt("SumScore") + sum_ave;
//						}
//						sum_ave = sum_ave + sum_qzp;
//
//						average = sum_ave / (list_members.size() + 1);// 互评的平均数，需要保存到PartySpiritCheck_SumAssesment表中
//					} else {
//						average = sum_qzp;
//					}

					// 计算totalScore
//					String sql_sum = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartySpiritCheck_SumAssesment] WHERE ParentID = ? AND PartyBranchID = ? AND Activity_ID = ? AND PartyMemberID_Assessed = ?";
//					PartySpiritCheck_SumAssesment SumAssesment = PartySpiritCheck_SumAssesment.dao.findFirst(sql_sum,
//							parentPartyId_static, partyBranchId__static, activityId_static, assessedId_static);
//
//					double totalScore = SumAssesment.getFloat("SelfAssessmentScore") * 0.2
//							+ SumAssesment.getFloat("MutualAssessScore") * 0.25 + average * 0.25
//							+ SumAssesment.getFloat("OrgAssessScore") * 0.3;
//					System.out.println("totalScore:" + dataFormat.doubleToTwo(totalScore));

					// 事务操作
//					String ss_ = ss;
//					JSONObject jsonObject = json_member;
//					System.out.println("ss_:" + ss_);
//					boolean succeed = Db.tx(new IAtom() {
//						public boolean run() throws SQLException {
//							count = 0,
//							if (jsonObject.getBoolean("IsFinished").equals(1)) {
//								flag = 1;
//								// 更新成绩表
//								if (list.size() > 0) {// 更新红线问题
//									String sql_sum = "SELECT * FROM [DB_PartySpritCheck_Test].[dbo].[PartySpiritCheck_SumAssesment]  WHERE ParentID = ? AND PartyBranchID = ? AND Activity_ID = ? AND PartyMemberID_Assessed = ?";
//									PartySpiritCheck_SumAssesment q = PartySpiritCheck_SumAssesment.dao.findFirst(
//											sql_sum, parentPartyId_static, partyBranchId__static, activityId_static,
//											assessedId_static);// 可以细化
//									System.out.println("q:---------------------" + q);
//									String a, b;
//									// 解决数据库中自带的字符串是'NULL'情况
//									if (q.getStr("RedLine_Mass") == null) {
//										a = ss_;
//									} else {
//										a = q.getStr("RedLine_Mass") + "," + ss_;
//									}
//									System.out.println("a:" + a);
//									if (q.getStr("RedLine_Sum") == null) {
//										b = ss_;
//									} else {
//										b = q.getStr("RedLine_Sum") + "," + ss_;
//									}
//
//									System.out.println("b:" + b);
//
//									count = Db.update(
//											"update [DB_PartySpritCheck_Test].[dbo].[PartySpiritCheck_SumAssesment] SET TotalScore = ?,MassAssessScore = ?,RedLine_Mass = ?,RedLine_Sum = ? WHERE  ParentID = ? AND PartyBranchID = ? AND Activity_ID = ? AND PartyMemberID_Assessed = ?",
//											totalScore, average, a, b, parentPartyId_static, partyBranchId__static,
//											activityId_static, assessedId_static);
//								} else {
//									count = Db.update(
//											"update [DB_PartySpritCheck_Test].[dbo].[PartySpiritCheck_SumAssesment] SET MassAssessScore = ?,TotalScore = ?  WHERE ParentID = ? AND PartyBranchID = ? AND Activity_ID = ? AND PartyMemberID_Assessed = ?",
//											average, totalScore, parentPartyId_static, partyBranchId__static,
//											activityId_static, assessedId_static);
//								}
//							}
							// 保存到答案表
//							boolean count2 = Db.save("[DB_PartySpritCheck_Test].[dbo].[Q_MassAssess_Answer_Replay]",
//									q_MassAssess_Answer_Replay);
//							System.out.println("count2" + count2);
//								
//							return count2 ;
//						}
//					});

					// 保存到答案表
					boolean count2 = Db.save("[DB_PartySpritCheck_Test].[dbo].[Q_MassAssess_Answer_Replay]",
							q_MassAssess_Answer_Replay);
					if (!count2)
						return (new JsonMsg(Code.C_107));

				}

			}
			// 更新用户表
//			Db.update(
//					"update [DB_PartySpritCheck_Test].[dbo].[MassAssessment_User] SET IsSubmit = ? WHERE ParentID = ? AND LoginID = ? AND PartyBranchID = ?",
//					2, parentPartyId_static, assessingId_static, partyBranchId__static);
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
						"update [DB_PartySpritCheck_Test].[dbo].[MassAssessment_User] SET IsSubmit = ? WHERE ParentID = ? AND LoginID = ? AND PartyBranchID = ?",
						2, parentPartyId_static, assessingId_static, partyBranchId__static);
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
		 return TtsMain.tts1(parentPartyId_static, "党性群众评端", s.getString("words"));
	 }
	
	

}
