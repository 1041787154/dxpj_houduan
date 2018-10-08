package com.dxpj.hp.controller;

import java.io.BufferedReader;

import com.alibaba.fastjson.JSONObject;
import com.dxpj.hp.server.HPserver;
import com.dxpj.server.comServer;
import com.dxpj.util.GetNumOfcomputer;
import com.dxpj.util.Video;
import com.dxpj.util.updateBackGround;
import com.jfinal.core.Controller;

public class HPController  extends Controller{

	
	
	// 服务层对象	 
	private HPserver hPserver = new HPserver();
	private Video video = new Video();
	private updateBackGround updatePict = new updateBackGround();
	/**<p>跳到首页
	 * 
	 */
	public void index() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		render("index.html");

	}
	
    /**<p>页面跳转<br>
     * 根据自己输入的网页的前缀，跳到指定页面<br>
     * URL=/dxpj/hp/to/<br>
     * page:跳转页面的名称
     * 
     */
	public void to() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		String page = getPara(0);
		renderJsp(page + ".html");
	}
	
/*	*//**<p>自定义提示音的语音播报功能<br>
	 * 简短的提示语用video_word(),长句子则放入文件中用video_file()读取<br>
	 * URL=/dxpj/hp/video_word?word=<br>
	 * word:自定义提示音
	 * 
	 *//*
	public void video_word() {
		String word = getPara("words");
		renderJson(zPserver.video_words(word));
	}

	*//**<p>对文件内容进行播音提示<br>
	 * 根据前端返回的文件名前缀，对文件内容进行播音<br>
	 * URL=/dxpj/hp/video_file?file=<br>
	 * file: 提示语句所在的文件的前缀
	 * @throws IOException  如果读取文件错误，报异常
	 * 
	 *//*
	public void video_file() throws IOException {  
		String fileName = getPara("file")+".txt";//fileName 提示语句所在的文件的前缀
		renderJson(zPserver.video_file(fileName));
   }  */
	
	
	//URL=/dxpj/zp/get_backBround?PictureTypeID=01&AssessTypeID=02
	
	public void getNumOfcomputer() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		String num = getPara("num");
		renderJson(GetNumOfcomputer.getNumOfcomputer(num));
	}
	
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
	
	
	
	
	//URL=/dxpj/hp/get_backBround?PictureTypeID=01&AssessTypeID=02
	
	public void get_backBround() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		String id_parentParty = getPara("parentId", "029-0001");//上级党组织id
		String id_pictureType = getPara("PictureTypeID");
		String id_assessType = getPara("AssessTypeID");
		renderJson(updatePict.get_backBround(id_parentParty,id_pictureType,id_assessType));
	}	
	/** <p>获得//党性自评端//渭阳西路街道党员党性教育体检中心；<br>
	 * URL=/dxpj/hp/get_A_Description<br>
	 *
	 *
	 */

	public void get_A_Description() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		String id_parentParty = getPara("parentId", "029-0001");//上级党组织id
		renderJson(hPserver.get_A_Description(id_parentParty));
	}	
	
	
	
	
	
	
	/**
	 * <p>在id号为id_parentParty的上级党组织内部，判断是否有活动；<br>
	 * URL=/dxpj/hp/judge_IsActivity?parentId=029-0001<br>
	 * parentId:上级组织ID
	 *
	 */

	public void judge_IsActivity() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		String id_parentParty = getPara("parentId", "029-0001");//上级党组织id
		renderJson(hPserver.judge_IsActivity(id_parentParty));
	}


	/**<p>获得有党性自评资格的党支部集合<br>
	 * URL=/dxpj/hp/get_party_selected<br>
	 * 
	 */
	public void get_party_selected() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		setAttr("partyBranchPage", hPserver.paginate_party(getParaToInt(0, 1), 3));
		renderJson(hPserver.get_party_selected());
	}

	/**
	 * <p>返回有党性评价资格的党支部内部的成员json集合<br>
	 * URL=/dxpj/hp/get_partyMember_byPartyId/04= <br>
	 * id_partyBranch:上级党组织下属的党支部id
	 * 
	 */

	public void get_partyMember_byPartyId() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		String id_partyBranch = getPara(0);
		setAttr("partyBranchPage", hPserver.paginate_member(getParaToInt(0, 1), 3,id_partyBranch));
		renderJson(hPserver.get_partyMember_byPartyId(id_partyBranch));
	}

	/**<p>检验用户信息， 如果正确，发送问题及相关信息集合<br>
	 * 
	 *  URL=/dxpj/hp/judge_partyMember_byMemberId?a=004&b=508886<br>
	 *  a: 党员编号                         <br>
	 *  b: 党员密码                        <br>
	 *                 
	 * 
	 */
	public void judge_partyMember_byMemberId() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		String id_partyMember = getPara("a");
		String pass = getPara("b");
		renderJson(hPserver.judge_partyMember_byMemberId(id_partyMember,pass));
	}

	/**<p>发送问题和相关信息集合<br>
	 * 根据上级党组织id、党支部内部的党员id、党支部id、 党员密码进行用户获取，<br>
	 * 其中上级党组织id目前只能是029-0001
	 * 
	 * URL=/dxpj/hp/get_questionAndAnswer<br>
	 * （返回结果包含问题、个人信息）
	 */
	public void get_questionAndAnswer() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		renderJson(hPserver.get_questionAndAnswer());
	}
	
	/**<p> 取Request中的数据对象(通用方法)
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

	/**<p>将答案和个人相关信息保存到数据库中<br>
	 *"URL=/dxpj/hp/save_questionAndAnswer"
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
		renderJson(hPserver.save_questionAndAnswer(s));

	
	}
	
	
	public void setAssessingStatus() {
		getResponse().addHeader("Access-Control-Allow-Origin", "*");
		renderJson(hPserver.setAssessingStatus());

	
	}
	
	/**<p>自定义提示音的语音播报功能<br>
	 * URL=/dxpj/hp/video_words<br>
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
		renderJson(hPserver.video_words(s));
	}
    
}
