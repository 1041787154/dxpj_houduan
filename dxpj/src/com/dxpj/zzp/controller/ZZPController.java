package com.dxpj.zzp.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.alibaba.fastjson.JSONObject;
import com.dxpj.qzp.server.QZPServer;
import com.dxpj.server.comServer;
import com.dxpj.util.DateUtils;
import com.dxpj.util.GetNumOfcomputer;
import com.dxpj.util.Video;
import com.dxpj.util.updateBackGround;
import com.dxpj.zp.server.ZPserver;
import com.dxpj.zzp.server.ZZPServer;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class ZZPController extends Controller{
	// 服务层对象
		private ZZPServer zZPserver = new ZZPServer();
		private Video video = new Video();
		private updateBackGround updatePict = new updateBackGround();
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
		 * URL=/dxpj/zzp/to/<br>
		 * page:跳转页面的名称
		 * 
		 */
		public void to() {
			getResponse().addHeader("Access-Control-Allow-Origin", "*");
			String page = getPara(0);
			renderJsp(page + ".html");
		}

	/*	*//**
		 * <p>
		 * 自定义提示音的语音播报功能<br>
		 * 简短的提示语用video_word(),长句子则放入文件中用video_file()读取<br>
		 * URL=/dxpj/zzp/video_word?word=<br>
		 * word:自定义提示音
		 * 
		 *//*
		public void video_word() {
			String word = getPara("words");
			renderJson(zPserver.video_word(word));
		}

		*//**
		 * <p>
		 * 对文件内容进行播音提示<br>
		 * 根据前端返回的文件名前缀，对文件内容进行播音<br>
		 * URL=/dxpj/zzp/video_file?file=<br>
		 * file: 提示语句所在的文件的前缀
		 * 
		 * @throws IOException
		 *             如果读取文件错误，报异常
		 * 
		 *//*
		public void video_file() throws IOException {
			String fileName = getPara("file") + ".txt";// fileName 提示语句所在的文件的前缀
			renderJson(zPserver.video_file(fileName));
		}
*/
		/** <p>获得//党性自评端//渭阳西路街道党员党性教育体检中心；<br>
		 * URL=/dxpj/zzp/get_ADescription<br>
		 *
		 *
		 */
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
		
		
		//URL=/dxpj/zzp/get_backBround?PictureTypeID=01&AssessTypeID=02
		
		public void get_backBround() {
			getResponse().addHeader("Access-Control-Allow-Origin", "*");
			String id_parentParty = getPara("parentId", "029-0001");//上级党组织id
			String id_pictureType = getPara("PictureTypeID");
			String id_assessType = getPara("AssessTypeID");
			renderJson(updatePict.get_backBround(id_parentParty,id_pictureType,id_assessType));
		}
		

		public void get_A_Description() {
			getResponse().addHeader("Access-Control-Allow-Origin", "*");
			String id_parentParty = getPara("parentId", "029-0001");//上级党组织id
			renderJson(zZPserver.get_A_Description(id_parentParty));
		}	
		
		/**
		 * <p>
		 * 在id号为id_parentParty的上级党组织内部，判断是否有活动；<br>
		 * URL=/dxpj/zzp/judge_IsActivity?parentId=029-0001<br>
		 * parentId:上级组织ID
		 *
		 */

		public void judge_IsActivity() {
			getResponse().addHeader("Access-Control-Allow-Origin", "*");
			String id_parentParty = getPara("parentId", "029-0001");// 上级党组织id
			renderJson(zZPserver.judge_IsActivity(id_parentParty));
		}

		/**
		 * <p> 参加党性评议的党支部集合<br>
		 * URL=/dxpj/zzp/get_party_selected<br>
		 * 
		 */
		public void get_party_selected() {
			getResponse().addHeader("Access-Control-Allow-Origin", "*");
			setAttr("partyBranchPage", zZPserver.paginate_party(getParaToInt(0, 1), 3));
			renderJson(zZPserver.get_party_selected());
		}

		

		/**
		 * 
		 * <p>检验用户信息， 如果正确，发送问题及相关信息集合<br>
		 * 
		 * URL=/dxpj/zzp/judge_partyMember_byMemberId?a=01&b=167903<br>
		 * a: 党支部编号 <br>
		 * b: 组织密码 <br>
		 * 
		 * 
		 */
		public void judge_partyMember_byMemberId() {
			getResponse().addHeader("Access-Control-Allow-Origin", "*");
			String id_partyBranch = getPara("a");
			String pass = getPara("b");
			renderJson(zZPserver.judge_partyMember_byMemberId(id_partyBranch, pass));
		}

		/**<p>发送问题和相关信息集合<br>
		 * 
		 * URL=/dxpj/zzp/get_questionAndAnswer<br>
		 * （返回结果包含问题、个人信息）
		 */
		public void get_questionAndAnswer() {
			getResponse().addHeader("Access-Control-Allow-Origin", "*");
			renderJson(zZPserver.get_questionAndAnswer());
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
		 * "URL=/dxpj/qzp/save_questionAndAnswer"
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
			renderJson(zZPserver.save_questionAndAnswer(s));

		}
//		/**
//		 * <p>将组员的成绩单打印出来
//		 */
//		public void getExcel () {		
//			renderFile(zZPserver.getExcel());
//		}
		/**<p>自定义提示音的语音播报功能<br>
		 * URL=/dxpj/zzp/video_words<br>
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
			renderJson(zZPserver.video_words(s));
		}
		
		
		

	}