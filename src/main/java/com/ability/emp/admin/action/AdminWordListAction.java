package com.ability.emp.admin.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ability.emp.admin.entity.AdminUserEntity;
import com.ability.emp.admin.entity.AdminWordEntity;
import com.ability.emp.admin.server.AdminUserService;
import com.ability.emp.admin.server.AdminWordService;
import com.ability.emp.util.ExcelImportUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;

@CrossOrigin // 解决跨域请求
@Controller
@RequestMapping("/admin/word")
public class AdminWordListAction {
	@Resource
	private AdminWordService wordService;

	ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 返回首页
	 * 
	 * @param wordEntity
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("")
	public String listPage(HttpServletRequest request, HttpServletResponse response) {
		return "wordlist";
	}

	/**
	 * 返回数据
	 * 
	 * @param wordEntity
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/queryAll")
	@ResponseBody
	public String queryAll(int pageSize, int pageNumber,String word,String interpretation) throws Exception {
		// 第一个参数当前页码，第二个参数每页条数
		PageHelper.startPage(pageNumber, pageSize);
		List<AdminWordEntity> data = wordService.queryWordAll(word,interpretation);
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> param = new HashMap<String, Object>();
		map.put("total", wordService.count(param));
		map.put("rows", data);
		return objectMapper.writeValueAsString(map);
	}

	/*
	 * @RequestMapping(value="/wordedit") public String wordedit(HttpServletRequest
	 * request,HttpServletResponse response){ return "wordedit"; }
	 */

	@RequestMapping(value = "/wordedit/{id}")
	@ResponseBody
	public String queryWordById(@PathVariable("id") String id) throws Exception {
		List<AdminWordEntity> word = wordService.queryWordById(id);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("word", word);
		return objectMapper.writeValueAsString(map);
	}

	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	@ResponseBody
	public String updateWord(String id, String word, String interpretation, String sentence, String mp3Name,
			String video, String img, String errInterpretation1, String errInterpretation2, String errInterpretation3) {
		AdminWordEntity wordEntity = new AdminWordEntity();
		wordEntity.setId(id);
		wordEntity.setWord(word);
		wordEntity.setInterpretation(interpretation);
		wordEntity.setSentence(sentence);
		wordEntity.setMp3Name(mp3Name);
		wordEntity.setVideo(video);
		wordEntity.setImg(img);
		wordEntity.setErrInterpretation1(errInterpretation1);
		wordEntity.setErrInterpretation2(errInterpretation2);
		wordEntity.setErrInterpretation3(errInterpretation3);
		wordService.update(wordEntity);
		return "wordlist";
	}

	/**
	 * 导入单词
	 * 
	 * @param file
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/importWord", method = RequestMethod.POST)
	public String importWord(@RequestParam(value = "file") MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		Map<String, Object> map = new HashMap<String, Object>();
		// 判断文件是否为空
		if (file == null) {
			map.put("msg", "文件不能为空！");
			return objectMapper.writeValueAsString(map);
		}

		// 获取文件名
		String fileName = file.getOriginalFilename();

		// 验证文件名是否合格
		if (!ExcelImportUtil.validateExcel(fileName)) {
			map.put("msg", "文件必须是excel格式！");
			return objectMapper.writeValueAsString(map);
		}

		// 进一步判断文件内容是否为空（即判断其大小是否为0或其名称是否为null）
		long size = file.getSize();
		if (fileName == null || fileName.equals("") || size == 0) {
			map.put("msg", "文件不能为空！");
			return objectMapper.writeValueAsString(map);
		}

		// 批量导入
		String message = wordService.importWord(fileName, file);
		map.put("msg", message);
		return "importusersuccess";
	}

	@RequestMapping("/importSuccess")
	public String importSuccess() throws Exception {
		return "importwordsuccess";
	}
}
