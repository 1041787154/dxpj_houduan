package com.dxpj.zp.controller;

import java.io.BufferedReader;
import com.alibaba.fastjson.JSONObject;
import com.dxpj.server.comServer;
import com.dxpj.util.GetNumOfcomputer;
import com.dxpj.util.changeSqlserver;
import com.dxpj.util.updateBackGround;
import com.dxpj.zp.server.ZPserver;
import com.jfinal.core.Controller;

//在规定的顺序下，判断是否有活动，不需要参数parentId
/**
 * <p>
 * 党性自评的系类操作<br>
 * 
 * 包含查询是否有激活的活动，查询有资格参加活动的党支部和党员，以及登录检验和问题的保存
 * 
 * @author huoyan
 * @since one
 *
 */
public class ZPController extends Controller {
	/**
	 * 服务层对象
	 */
	private ZPserver zPserver = new ZPserver();
	private updateBackGround updatePict = new updateBackGround();
	private changeSqlserver change = new changeSqlserver();
//	private Video video = new Video();

	/**
	 * <p>
	 * 跳到首页
	 * 
	 */
	public void index() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		render("index.html");
	}

	/**
	 * <p>
	 * 页面跳转<br>
	 * 根据自己输入的网页的前缀，跳到指定页面<br>
	 * URL=/dxpj/zp/to/<br>
	 * page:跳转页面的名称
	 * 
	 */
	public void to() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		String page = getPara(0);
		renderJsp(page + ".html");
	}

	// URL=/dxpj/zp/get_backBround?PictureTypeID=01&AssessTypeID=02

	public void register_() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		String user = getPara("user");
		String phone = getPara("phone");
		renderJson(comServer.register_(user, phone));
	}

	public void judge_assess() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");

		renderJson(comServer.judge_IsAssess());
	}

	// URL=/dxpj/zp/get_backBround?PictureTypeID=01&AssessTypeID=02

	public void get_backBround() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		String id_parentParty = getPara("parentId", "029-0001");// 上级党组织id
		String id_pictureType = getPara("PictureTypeID");
		String id_assessType = getPara("AssessTypeID");
		setSessionAttr("id_parentParty", id_parentParty);
		setSessionAttr("id_assessType", id_assessType);

		renderJson(updatePict.get_backBround(id_parentParty, id_pictureType, id_assessType));
	}

	/**
	 * <p>
	 * 获得//党性自评端//渭阳西路街道党员党性教育体检中心；<br>
	 * URL=/dxpj/zp/get_ADescription<br>
	 *
	 *
	 */

	public void get_A_Description() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		String id_parentParty = getPara("parentId", "029-0001");// 上级党组织id
		renderJson(zPserver.get_A_Description(id_parentParty));
	}

	/**
	 * <p>
	 * 在id号为id_parentParty的上级党组织内部，判断是否有活动；<br>
	 * URL=/dxpj/zp/judge_IsActivity?parentId=029-0001<br>
	 * parentId:上级组织ID
	 *
	 */

	public void judge_IsActivity() {
		String id_parentParty = getPara("parentId", "029-0001");// 上级党组织id
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		renderJson(zPserver.judge_IsActivity(id_parentParty));
	}

	/**
	 * <p>
	 * 获得//不忘初心、牢记使命，增强党性、提升素养。欢迎您来到渭阳西路街道党员党性教育体检中心，我是本次
	 * “党性体检”语音提示小助手，本次党性体检流程由“党员自评”“党员互评”“群众评议”“组织评议”四个环节组成。<br>
	 * URL=/dxpj/zp/get_generalDescription<br>
	 *
	 *
	 */

	public void get_generalDescription() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		renderJson(zPserver.get_generalDescription());
	}

	/**
	 * <p>
	 * 获得有党性自评资格的党支部集合<br>
	 * URL=/dxpj/zp/get_party_selected<br>
	 * 
	 */
	public void get_party_selected() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		setAttr("partyBranchPage", zPserver.paginate_party(getParaToInt(0, 1), 3));
		renderJson(zPserver.get_party_selected());
	}

	/**
	 * <p>
	 * 返回有党性评价资格的党支部内部的成员json集合<br>
	 * URL=/dxpj/zp/get_partyMember_byPartyId/11 <br>
	 * 
	 */

	public void get_partyMember_byPartyId() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		String id_partyBranch = getPara(0);
		setAttr("partyBranchPage", zPserver.paginate_member(getParaToInt(0, 1), 3, id_partyBranch));
		renderJson(zPserver.get_partyMember_byPartyId(id_partyBranch));
	}

	/**
	 * <p>
	 * 检验用户信息， 如果正确，发送问题集合<br>
	 * 
	 * 判断是否验证正确，如果正确，则返回相关信息，（信息验证正确，则首先先看该用户是否有以前的自评记录，通过 "idHavingWrited":
	 * "1"查看，然后再根据具体的状态进行相应的值得返回）<br>
	 * "URL=/dxpj/zp/judge_partyMember_byMemberId?a=013&b=两新组织党员&c=167903"<br>
	 * a: 党员编号 <br>
	 * b: 所属党派类别名称 <br>
	 * c: 党员密码
	 * 
	 */
	public void judge_partyMember_byMemberId() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		String id_partyMember = getPara("a");
		String partyType = getPara("b");
		String pass = getPara("c");
		renderJson(zPserver.judge_partyMember_byMemberId(id_partyMember, partyType, pass));
	}

	/**
	 * <p>
	 * 发送问题和相关信息集合<br>
	 * 根据上级党组织id、党支部内部的党员id、党支部id、党支部所属的党派、 党员密码进行用户获取，<br>
	 * 其中上级党组织id目前只能是029-0001
	 * 
	 * URL=/dxpj/zp/get_questionAndAnswer<br>
	 * （返回结果包含问题、个人信息、有自评记录的话，也会返回自评记录）
	 */
	public void get_questionAndAnswer() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		renderJson(zPserver.get_questionAndAnswer());
	}

	/**
	 * <p>
	 * 取Request中的数据对象(通用方法)
	 * 
	 * @return JSONObject 数据对象
	 * @throws Exception
	 */
	protected JSONObject getRequestObject() throws Exception {
		StringBuilder json = new StringBuilder();
		BufferedReader reader = this.getRequest().getReader();
		String line = null;
		while ((line = reader.readLine()) != null) {
			json.append(line);
		}
		reader.close();
		return JSONObject.parseObject(json.toString());
	}

	/**
	 * <p>
	 * 将答案和个人相关信息保存到数据库中<br>
	 * "URL=/dxpj/zp/save_questionAndAnswer"
	 * 
	 */

	public void save_questionAndAnswer() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		JSONObject s = null;
		try {
			s = this.getRequestObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		renderJson(zPserver.save_questionAndAnswer(s));

	}

//	/**<p>自定义提示音的语音播报功能<br>
//	 * URL=/dxpj/zp/video_words<br>
//	 * 
//	 * 
//	 */
//	public void video_words() {
//		getResponse().addHeader("Access-Control-Allow-Origin", "*");
//		JSONObject s = null;
//		try {
//			s = this.getRequestObject();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		renderJson(video.video_words(s));
//	}
//	
//	/**<p>关闭自定义提示音的语音播报功能<br>
//	 * URL=/dxpj/zp/close_video<br>
//	 * 
//	 * 
//	 */
//	public void close_video() {
//		getResponse().addHeader("Access-Control-Allow-Origin", "*");
//		renderJson(video.close_video());
//	}

	/**
	 * <p>
	 * 自定义提示音的语音播报功能<br>
	 * URL=/dxpj/zp/video_words?words='dfsdfsdfsdfs'<br>
	 * 
	 * @throws Exception
	 * 
	 * 
	 */
	public void video_words() throws Exception {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		JSONObject s = null;
		try {
			s = this.getRequestObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
//		renderJson(zPserver.video_words(s));
		renderJson(zPserver.video_words(s));
//		renderJson();
	}

	public void temptA() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		renderJson(change.decMp3());

	}

}
