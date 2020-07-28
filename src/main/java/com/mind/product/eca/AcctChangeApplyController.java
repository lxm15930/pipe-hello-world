package com.mind.product.eca.am.acct.change.apply;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.mind.platform.system.base.CMReturn;
import com.mind.platform.system.base.Page;
import com.mind.platform.system.base.UserToken;
import com.mind.platform.system.conf.Interlization;
import com.mind.platform.system.utils.DateUtils;
import com.mind.product.eca.am.acct.change.apply.api.IAcctChangeApplyService;
import com.mind.product.eca.am.acct.change.apply.entity.AcctAdjustApply;
import com.mind.product.eca.am.acct.change.apply.entity.AcctCancelApply;
import com.mind.product.eca.am.acct.change.apply.entity.AcctChangeApply;
import com.mind.product.eca.am.acct.change.apply.entity.AcctChangeApplySearchCond;
import com.mind.product.eca.am.acct.change.apply.entity.AcctChangeType;
import com.mind.product.eca.am.acct.change.apply.entity.BankAcctSearchCode;
import com.mind.product.eca.cd.bank.entity.Bank;
import com.mind.product.eca.cd.currency.entity.Currency;
import com.mind.product.eca.cm.exportexcel.api.ExcelDataSource;
import com.mind.product.eca.cm.exportexcel.entity.ColDefEntity;
import com.mind.product.eca.cm.exportexcel.entity.XlsxExport;
import com.mind.product.eca.cm.importexcel.entity.Excel;
import com.mind.product.eca.cm.importexcel.entity.ExcelImport;
import com.mind.product.eca.cm.lookup.banklookup.api.IBankLookUpService;
import com.mind.product.eca.cm.lookup.banklookup.entity.BankLookUp;
import com.mind.product.eca.cm.lookup.banklookup.entity.BankLookUpSearchCond;
import com.mind.product.eca.system.common.dataAr.DataAr;
import com.mind.product.eca.system.login.User;
import com.mind.product.eca.tm.payout.common.HouseBank;

@Controller
@RequestMapping("eca/am/AcctChangeApply/")
@RequiresPermissions("AcctChangeApply:mng")
public class AcctChangeApplyController {
	private final static Logger logger = LoggerFactory.getLogger(AcctChangeApplyController.class);
	@Autowired//1.0
	IAcctChangeApplyService aAcctChangeApplyService;
	@Autowired//1.0
    IBankLookUpService iBankservice;
	@RequestMapping("list")
	public String list(HttpServletRequest aRequest, HttpSession aSession, String originFlag, Model aModel,
			@ModelAttribute(value = "aAcctChangeApplySearchCond") @Valid AcctChangeApplySearchCond aCond,
			BindingResult bindingResult, Integer aPageNo, UserToken aUserToken) {
		if ("1".equalsIgnoreCase(originFlag)) {
			aPageNo = (Integer) aRequest.getSession().getAttribute("aAcctChangeApplyListPageNo");
			aCond = (AcctChangeApplySearchCond) aRequest.getSession().getAttribute("aAcctChangeApplySearchCond");
		}
		aPageNo = aPageNo == null || aPageNo == 0 ? 1 : aPageNo;
		User oUser = (User) aRequest.getSession().getAttribute("user");
		aUserToken = oUser.getUserToken();
		Page<AcctChangeApply> oRtn = aAcctChangeApplyService.queryPage(aCond, aPageNo, aUserToken);
		aModel.addAttribute("pageData", oRtn);
		 if(aCond != null) {
			aModel.addAttribute("aAcctChangeApplySearchCond", aCond);
		 }
		aSession.setAttribute("aAcctChangeApplyListPageNo", aPageNo);
		aSession.setAttribute("aAcctChangeApplySearchCond", aCond);
		return "eca/am/acct/change/apply/AcctChangeApplyList";
	}

	@RequestMapping("gotoAddEdit")//去新增页面 
	public String gotoAddEdit(HttpServletRequest aRequest, Model aModel, @ModelAttribute(value = "aAcctChangeApply") @Valid AcctChangeApply aAcctChangeApply, BindingResult bindingResult, UserToken aUserToken) {
		User oUser = (User) aRequest.getSession().getAttribute("user");
		aUserToken = oUser.getUserToken();
		if (aAcctChangeApply.getChange_type() == null && aAcctChangeApply.getRow_id() > 0) {
		  aAcctChangeApply = aAcctChangeApplyService.getBean(aAcctChangeApply.getRow_id(),aUserToken);
		  if ("2".equals(aAcctChangeApply.getChange_type())) {
		    return "redirect:gotoAdjustAddEdit?row_id=" + aAcctChangeApply.getRow_id();
		  } else if ("3".equals(aAcctChangeApply.getChange_type())) {
	        return "redirect:gotoCancelAddEdit?row_id=" + aAcctChangeApply.getRow_id();
	      }
		}
		List<AcctChangeType> changeType = aAcctChangeApplyService.queryAcctChangeType();
		aModel.addAttribute("changeTypes", changeType);
		List<Bank> Banks=aAcctChangeApplyService.getBank(aUserToken);
		aModel.addAttribute("banks", Banks);
		List<HouseBank> province = aAcctChangeApplyService.queryProvince();
		aModel.addAttribute("province", province);
		List<Currency> Currencys=aAcctChangeApplyService.getCurrency(aUserToken);
		aModel.addAttribute("currencys", Currencys);
		
		if (aAcctChangeApply.getRow_id() > 0) {//修改页面
			aAcctChangeApply = aAcctChangeApplyService.getBean(aAcctChangeApply.getRow_id(),aUserToken);
			aModel.addAttribute("aAcctChangeApply", aAcctChangeApply);
		}else {
			aAcctChangeApply.setPurpose("");
		}
		aModel.addAttribute("oRtn", new CMReturn(1));
		return "eca/am/acct/change/apply/AcctChangeApplyAddEdit";
	}
	@RequestMapping("bankLookUp")
    public String bankLookUp(HttpServletRequest aRequest,Model aModel,@ModelAttribute(value = "aCond") BankLookUpSearchCond aCond,Integer aPageNo,UserToken aUserToken) {
	  aPageNo = aPageNo == null || aPageNo == 0 ? 1 : aPageNo;
      User oUser = (User)aRequest.getSession().getAttribute("user");
      aUserToken = oUser.getUserToken();
      Page<BankLookUp> oRtn = iBankservice.querySysBank(aCond, aPageNo, aUserToken);
      aModel.addAttribute("pageData", oRtn);
      return "eca/system/common/lookUp/bank/bankLookUp";
    }
	@RequestMapping("gotoAdjustAddEdit")//去新增页面 
	public String gotoAdjustAddEdit(HttpServletRequest aRequest, Model aModel, @ModelAttribute(value = "aAcctAdjustApply") @Valid  AcctAdjustApply aAcctAdjustApply, BindingResult bindingResult, UserToken aUserToken) {
	  User oUser = (User) aRequest.getSession().getAttribute("user");
	  aUserToken = oUser.getUserToken();
	  
	  List<AcctChangeType> changeType = aAcctChangeApplyService.queryAcctChangeType();
	  aModel.addAttribute("changeTypes", changeType);
	  if (aAcctAdjustApply.getRow_id() > 0) {//修改页面
	    AcctChangeApply aAcctChangeApply = aAcctChangeApplyService.getBean(aAcctAdjustApply.getRow_id(),aUserToken);
	    aAcctAdjustApply = convertBean(aAcctChangeApply, aAcctAdjustApply.getClass());
	    aModel.addAttribute("aAcctAdjustApply", aAcctAdjustApply);
	  }else {
		  aAcctAdjustApply.setPurpose("");
	  }
	  aModel.addAttribute("oRtn", new CMReturn(1));
	  return "eca/am/acct/change/apply/AcctAdjustApplyAddEdit";
	}
	@RequestMapping("gotoCancelAddEdit")//去新增页面 
	public String gotoCancelAddEdit(HttpServletRequest aRequest, Model aModel, @ModelAttribute(value = "aAcctCancelApply") @Valid AcctCancelApply aAcctCancelApply, BindingResult bindingResult, UserToken aUserToken) {
	  User oUser = (User) aRequest.getSession().getAttribute("user");
	  aUserToken = oUser.getUserToken();
	  
	  List<AcctChangeType> changeType = aAcctChangeApplyService.queryAcctChangeType();
	  aModel.addAttribute("changeTypes", changeType);
	  if (aAcctCancelApply.getRow_id() > 0) {//修改页面
	    AcctChangeApply aAcctChangeApply = aAcctChangeApplyService.getBean(aAcctCancelApply.getRow_id(),aUserToken);
	    aAcctCancelApply = convertBean(aAcctChangeApply, aAcctCancelApply.getClass());
	    aModel.addAttribute("aAcctCancelApply", aAcctCancelApply);
	  }else {
		  aAcctCancelApply.setPurpose("");
	  }
	  aModel.addAttribute("oRtn", new CMReturn(1));
	  return "eca/am/acct/change/apply/AcctCancelApplyAddEdit";
	}

	@RequestMapping("addEdit")
	public String addEdit(HttpServletRequest aRequest, Model aModel, @ModelAttribute(value = "aAcctChangeApply") @Valid AcctChangeApply aAcctChangeApply, BindingResult bindingResult, UserToken aUserToken) {
		User oUser = (User) aRequest.getSession().getAttribute("user");
		aUserToken = oUser.getUserToken();
		String[] errorMsg = new String[] {"change_name","company_name","bank_name","currency_name","conn_flag","purpose","open_company_name","account_code","account_name","hbank_name","hbank_code","pay_flag"};
		aModel.addAttribute("errorMsg", errorMsg);
		List<AcctChangeType> changeType = aAcctChangeApplyService.queryAcctChangeType();
		aModel.addAttribute("changeTypes", changeType);
		List<Bank> Banks=aAcctChangeApplyService.getBank(aUserToken);
		aModel.addAttribute("banks", Banks);
		List<HouseBank> province = aAcctChangeApplyService.queryProvince();
		aModel.addAttribute("province", province);
		List<Currency> Currencys=aAcctChangeApplyService.getCurrency(aUserToken);
		aModel.addAttribute("currencys", Currencys);
		
		CMReturn oRtn = new CMReturn(1);
		if (bindingResult.hasErrors()) {
			aModel.addAttribute("oRtn", oRtn);
			return "eca/am/acct/change/apply/AcctChangeApplyAddEdit";
		}
		CMReturn dataAr = new DataAr().getDataAr(aAcctChangeApply.getCompany_id());
		if (dataAr.getValue() < 0) {
			return dataAr.getMsg();
		}
		if (aAcctChangeApply.getRow_id() > 0) {
			oRtn = aAcctChangeApplyService.update(aAcctChangeApply, aUserToken);
		} else {
			oRtn = aAcctChangeApplyService.add(aAcctChangeApply, aUserToken);
		}
		if (oRtn.getValue() < 0) {
			aModel.addAttribute("oRtn", oRtn);
			return "eca/am/acct/change/apply/AcctChangeApplyAddEdit";
		}
		return "redirect:list?originFlag=1";
	}
	@RequestMapping("addEditAdjust")
	public String addEditAdjust(HttpServletRequest aRequest, Model aModel, @ModelAttribute(value = "aAcctAdjustApply") @Valid AcctAdjustApply aAcctAdjustApply, BindingResult bindingResult, UserToken aUserToken) {
	  User oUser = (User) aRequest.getSession().getAttribute("user");
	  aUserToken = oUser.getUserToken();
	  String[] errorMsg = new String[] {"change_type","purpose","account_name","account_name_new","account_code"};
	  aModel.addAttribute("errorMsg", errorMsg);
	  
	  List<AcctChangeType> changeType = aAcctChangeApplyService.queryAcctChangeType();
	  aModel.addAttribute("changeTypes", changeType);
	  
	  CMReturn oRtn = new CMReturn(1);
	  if (bindingResult.hasErrors()) {
	    aModel.addAttribute("oRtn", oRtn);
	    return "eca/am/acct/change/apply/AcctAdjustApplyAddEdit";
	  }
	  CMReturn dataAr = new DataAr().getDataAr(aAcctAdjustApply.getCompany_id());
		if (dataAr.getValue() < 0) {
			return dataAr.getMsg();
	  }
	  AcctChangeApply aAcctChangeApply = new AcctChangeApply();
	  aAcctChangeApply = convertBean(aAcctAdjustApply, aAcctChangeApply.getClass());
	  if (aAcctAdjustApply.getRow_id() > 0) {
	    oRtn = aAcctChangeApplyService.update(aAcctChangeApply, aUserToken);
	  } else {
	    oRtn = aAcctChangeApplyService.add(aAcctChangeApply, aUserToken);
	  }
	  if (oRtn.getValue() < 0) {
	    aModel.addAttribute("oRtn", oRtn);
	    return "eca/am/acct/change/apply/AcctAdjustApplyAddEdit";
	  }
	  return "redirect:list?originFlag=1";
	}
	@RequestMapping("addEditCancel")
	public String addEditCancel(HttpServletRequest aRequest, Model aModel, @ModelAttribute(value = "aAcctCancelApply") @Valid AcctCancelApply aAcctCancelApply, BindingResult bindingResult, UserToken aUserToken) {
	  User oUser = (User) aRequest.getSession().getAttribute("user");
	  aUserToken = oUser.getUserToken();
	  String[] errorMsg = new String[] {"change_type","purpose","account_code"};
	  aModel.addAttribute("errorMsg", errorMsg);
	  
	  List<AcctChangeType> changeType = aAcctChangeApplyService.queryAcctChangeType();
	  aModel.addAttribute("changeTypes", changeType);
	  
	  CMReturn oRtn = new CMReturn(1);
	  if (bindingResult.hasErrors()) {
	    aModel.addAttribute("oRtn", oRtn);
	    return "eca/am/acct/change/apply/AcctCancelApplyAddEdit";
	  }
	  CMReturn dataAr = new DataAr().getDataAr(aAcctCancelApply.getCompany_id());
		if (dataAr.getValue() < 0) {
			return dataAr.getMsg();
	  }
	  AcctChangeApply aAcctChangeApply = new AcctChangeApply();
	  aAcctChangeApply = convertBean(aAcctCancelApply, aAcctChangeApply.getClass());
	  if (aAcctCancelApply.getRow_id() > 0) {
	    oRtn = aAcctChangeApplyService.update(aAcctChangeApply, aUserToken);
	  } else {
	    oRtn = aAcctChangeApplyService.add(aAcctChangeApply, aUserToken);
	  }
	  if (oRtn.getValue() < 0) {
	    aModel.addAttribute("oRtn", oRtn);
	    return "eca/am/acct/change/apply/AcctCancelApplyAddEdit";
	  }
	  return "redirect:list?originFlag=1";
	}
	
	@RequestMapping("gotoDetail")
	public String gotoDetail(HttpServletRequest aRequest,Model aModel, Long row_id, UserToken aUserToken) {
		User oUser = (User) aRequest.getSession().getAttribute("user");
		aUserToken = oUser.getUserToken();
		AcctChangeApply aAcctChangeApply = aAcctChangeApplyService.getBean(row_id,aUserToken);
		aModel.addAttribute("aAcctChangeApply", aAcctChangeApply);
		return "eca/am/acct/change/apply/AcctChangeApplyDetail";
	}

	  @RequestMapping("gotoAcctChangeDetail")
	  public String AcctChangeDetail(HttpServletRequest aRequest,Model aModel,Long row_id, UserToken aUserToken) {
		User oUser = (User) aRequest.getSession().getAttribute("user");
		aUserToken = oUser.getUserToken();
		AcctChangeApply acctChangeApply = aAcctChangeApplyService.get(row_id,aUserToken);
	    aModel.addAttribute("acctChangeApply", acctChangeApply);
	    return "eca/am/acct/change/apply/AcctChangeApplyDetail";
	  }
	
	@RequestMapping("getParents")
	@ResponseBody
	public List<HouseBank> getParents(HttpServletRequest aRequest, Model aModel, String province_code, UserToken aUserToken) {
		User oUser = (User) aRequest.getSession().getAttribute("user");
		aUserToken = oUser.getUserToken();
		Map<String, List<HouseBank>> parents = new HashMap<String, List<HouseBank>>();
		if (province_code != null) {
			List<HouseBank> parent = aAcctChangeApplyService.queryParentByProvince(province_code);	
			parents.put(province_code, parent);
		}
		return parents.get(province_code);
	}
	
	@RequestMapping("hbankLookUp")
	public String recAccountLookUp(HttpServletRequest aRequest, Model aModel, String bank_code, @ModelAttribute("aCond") HouseBank aCond, Integer aPageNo, UserToken aUserToken) {
	    aPageNo = aPageNo == null || aPageNo == 0 ? 1 : aPageNo;
        User oUser = (User)aRequest.getSession().getAttribute("user");
        aUserToken = oUser.getUserToken();
        List<HouseBank> province = aAcctChangeApplyService.queryProvince();
        aModel.addAttribute("province", province);
        Page<HouseBank> oRtn = aAcctChangeApplyService.lookUpHouseBankPage(aPageNo, aCond, bank_code, aUserToken);
        aModel.addAttribute("pageData", oRtn);
        aModel.addAttribute("aCond", aCond);
		return "eca/am/acct/change/apply/hbankLookUp";
	}
	
	@RequestMapping("acctLookUp")
  public String recAcctLookUp(HttpServletRequest aRequest, Model aModel, @ModelAttribute(value = "aCond") BankAcctSearchCode aCond, Integer aPageNo, UserToken aUserToken) {
	aPageNo = aPageNo == null || aPageNo == 0 ? 1 : aPageNo;
    User oUser = (User)aRequest.getSession().getAttribute("user");
    aUserToken = oUser.getUserToken();
    Page<AcctChangeApply> oRtn = aAcctChangeApplyService.queryAccountPage(aCond, aPageNo, aUserToken);
    aModel.addAttribute("pageData", oRtn);
    aModel.addAttribute("lookUp", "acctLookUp");
    return "eca/am/acct/change/apply/AccountLookUp";
  }
	@RequestMapping("saveSubmit")
	public String saveSubmit(HttpServletRequest aRequest, Model aModel,
			@ModelAttribute(value = "aAcctChangeApply") @Valid AcctChangeApply aAcctChangeApply,
			BindingResult bindingResult, UserToken aUserToken) {
		User oUser = (User) aRequest.getSession().getAttribute("user");
		aUserToken = oUser.getUserToken();
		
		List<AcctChangeType> changeType = aAcctChangeApplyService.queryAcctChangeType();
    aModel.addAttribute("changeTypes", changeType);
		List<Bank> Banks=aAcctChangeApplyService.getBank(aUserToken);
		aModel.addAttribute("banks", Banks);
		List<HouseBank> province = aAcctChangeApplyService.queryProvince();
		aModel.addAttribute("province", province);
		List<Currency> Currencys=aAcctChangeApplyService.getCurrency(aUserToken);
		aModel.addAttribute("currencys", Currencys);
		CMReturn oRtn = new CMReturn(1); 
		String[] errorMsg = new String[] {"change_name","company_name","bank_name","currency_name","conn_flag","purpose","open_company_name","account_code","account_name","hbank_name","hbank_code","pay_flag"};
    aModel.addAttribute("errorMsg", errorMsg);
		if (bindingResult.hasErrors()) {
			aModel.addAttribute("oRtn", oRtn);
			return "eca/am/acct/change/apply/AcctChangeApplyAddEdit";
		}
		long rowId = 0;
		if (aAcctChangeApply.getRow_id() > 0) {
			oRtn = aAcctChangeApplyService.update(aAcctChangeApply, aUserToken);
			rowId = aAcctChangeApply.getRow_id();
		} else {
			oRtn = aAcctChangeApplyService.add(aAcctChangeApply, aUserToken);
			if (oRtn.getValue() > 0) {
				rowId = Long.valueOf(oRtn.getReturnPara("rowId", "").toString());
				aAcctChangeApply.setRow_id(rowId);
			}
		}
		aModel.addAttribute("oRtn", oRtn);
		aModel.addAttribute("rowId", rowId);
		return "eca/am/acct/change/apply/AcctChangeApplyAddEdit";
	}
	@RequestMapping("saveSubmitAdjust")
	public String saveSubmitAdjust(HttpServletRequest aRequest, Model aModel,
	    @ModelAttribute(value = "aAcctAdjustApply") @Valid AcctAdjustApply aAcctAdjustApply,
	    BindingResult bindingResult, UserToken aUserToken) {
	  User oUser = (User) aRequest.getSession().getAttribute("user");
	  aUserToken = oUser.getUserToken();
	  
	  List<AcctChangeType> changeType = aAcctChangeApplyService.queryAcctChangeType();
	  aModel.addAttribute("changeTypes", changeType);
	  List<Bank> Banks=aAcctChangeApplyService.getBank(aUserToken);
	  aModel.addAttribute("banks", Banks);
	  List<HouseBank> province = aAcctChangeApplyService.queryProvince();
	  aModel.addAttribute("province", province);
	  List<Currency> Currencys=aAcctChangeApplyService.getCurrency(aUserToken);
	  aModel.addAttribute("currencys", Currencys);
	  CMReturn oRtn = new CMReturn(1);
	  String[] errorMsg = new String[] {"change_type","purpose","account_name","account_name_new","account_code"};
	  aModel.addAttribute("errorMsg", errorMsg);
	  if (bindingResult.hasErrors()) {
	    aModel.addAttribute("oRtn", oRtn);
	    return "eca/am/acct/change/apply/AcctAdjustApplyAddEdit";
	  }
	  AcctChangeApply aAcctChangeApply = new AcctChangeApply();
    aAcctChangeApply = convertBean(aAcctAdjustApply, aAcctChangeApply.getClass());
	  long rowId = 0;
	  if (aAcctAdjustApply.getRow_id() > 0) {
	    oRtn = aAcctChangeApplyService.update(aAcctChangeApply, aUserToken);
	    rowId = aAcctChangeApply.getRow_id();
	  } else {
	    oRtn = aAcctChangeApplyService.add(aAcctChangeApply, aUserToken);
	    if (oRtn.getValue() > 0) {
	      rowId = Long.valueOf(oRtn.getReturnPara("rowId", "").toString());
	      aAcctChangeApply.setRow_id(rowId);
	    }
	  }
	  aModel.addAttribute("oRtn", oRtn);
	  aModel.addAttribute("rowId", rowId);
	  return "eca/am/acct/change/apply/AcctAdjustApplyAddEdit";
	}
	@RequestMapping("saveSubmitCancel")
	public String saveSubmitCancel(HttpServletRequest aRequest, Model aModel,
	    @ModelAttribute(value = "aAcctCancelApply") @Valid AcctCancelApply aAcctCancelApply,
	    BindingResult bindingResult, UserToken aUserToken) {
	  User oUser = (User) aRequest.getSession().getAttribute("user");
	  aUserToken = oUser.getUserToken();
	  
	  List<AcctChangeType> changeType = aAcctChangeApplyService.queryAcctChangeType();
	  aModel.addAttribute("changeTypes", changeType);
	  List<Bank> Banks=aAcctChangeApplyService.getBank(aUserToken);
	  aModel.addAttribute("banks", Banks);
	  List<HouseBank> province = aAcctChangeApplyService.queryProvince();
	  aModel.addAttribute("province", province);
	  List<Currency> Currencys=aAcctChangeApplyService.getCurrency(aUserToken);
	  aModel.addAttribute("currencys", Currencys);
	  CMReturn oRtn = new CMReturn(1);
	  String[] errorMsg = new String[] {"change_type","purpose","account_code"};
	  aModel.addAttribute("errorMsg", errorMsg);
	  if (bindingResult.hasErrors()) {
	    aModel.addAttribute("oRtn", oRtn);
	    return "eca/am/acct/change/apply/AcctCancelApplyAddEdit";
	  }
	  AcctChangeApply aAcctChangeApply = new AcctChangeApply();
	  aAcctChangeApply = convertBean(aAcctCancelApply, aAcctChangeApply.getClass());
	  long rowId = 0;
	  if (aAcctCancelApply.getRow_id() > 0) {
	    oRtn = aAcctChangeApplyService.update(aAcctChangeApply, aUserToken);
	    rowId = aAcctChangeApply.getRow_id();
	  } else {
	    oRtn = aAcctChangeApplyService.add(aAcctChangeApply, aUserToken);
	    if (oRtn.getValue() > 0) {
	      rowId = Long.valueOf(oRtn.getReturnPara("rowId", "").toString());
	      aAcctChangeApply.setRow_id(rowId);
	    }
	  }
	  aModel.addAttribute("oRtn", oRtn);
	  aModel.addAttribute("rowId", rowId);
	  return "eca/am/acct/change/apply/AcctCancelApplyAddEdit";
	}
	
	@RequestMapping("cancel")
	@ResponseBody
	public CMReturn cancel(HttpServletRequest aRequest, long[] aIds, UserToken aUserToken) {
		User oUser = (User) aRequest.getSession().getAttribute("user");
		aUserToken = oUser.getUserToken();
		CMReturn oRtn = aAcctChangeApplyService.cancel(aIds, aUserToken);
		if (oRtn.getMsgCode() != null && !"".equalsIgnoreCase(oRtn.getMsgCode())) {
			String sMsg = Interlization.getMessage(aUserToken.getLang_type(), oRtn.getMsgCode());
			Object[] params = { oRtn.getReturnPara("0", ""), oRtn.getReturnPara("1", "") };
			MessageFormat mf = new MessageFormat(sMsg);
			String sContent = mf.format(params);
			oRtn.setValue(oRtn.getValue(), sContent);
		}
		return oRtn;
	}

	@RequestMapping("submit")
	@ResponseBody
	public CMReturn submit(HttpServletRequest aRequest, long[] aIds, UserToken aUserToken) {
		User oUser = (User) aRequest.getSession().getAttribute("user");
		aUserToken = oUser.getUserToken();
		CMReturn oRtn = aAcctChangeApplyService.submit(aIds, aUserToken);
		if (oRtn.getMsgCode() != null && !"".equalsIgnoreCase(oRtn.getMsgCode())) {
			String sMsg = Interlization.getMessage(aUserToken.getLang_type(), oRtn.getMsgCode());
			Object[] params = { oRtn.getReturnPara("0", ""), oRtn.getReturnPara("1", "") };
			MessageFormat mf = new MessageFormat(sMsg);
			String sContent = mf.format(params);
			oRtn.setValueCode(oRtn.getValue(), sContent);
		}
		return oRtn;
	}

	@RequestMapping("submitOne")
	public String submitOne(HttpServletRequest aRequest, Model aModel,
			@ModelAttribute(value = "aAcctChangeApply") AcctChangeApply aAcctChangeApply, UserToken aUserToken) {
		User oUser = (User) aRequest.getSession().getAttribute("user");
		aUserToken = oUser.getUserToken();
		CMReturn oRtn = aAcctChangeApplyService.submit(aAcctChangeApply.getRow_id(), aUserToken);
		if (oRtn.getValue() < 0) {
		  List<AcctChangeType> changeType = aAcctChangeApplyService.queryAcctChangeType();
	    aModel.addAttribute("changeTypes", changeType);
			List<Bank> Banks=aAcctChangeApplyService.getBank(aUserToken);
			aModel.addAttribute("banks", Banks);
			List<HouseBank> province = aAcctChangeApplyService.queryProvince();
			aModel.addAttribute("province", province);
			List<Currency> Currencys=aAcctChangeApplyService.getCurrency(aUserToken);
			aModel.addAttribute("currencys", Currencys);
			aModel.addAttribute("oRtn", oRtn);
			return "eca/am/acct/change/apply/AcctChangeApplyAddEdit";
		}
		return "redirect:list?originFlag=1";
	}
	@RequestMapping("submitOneAdjust")
	public String submitOneAdjust(HttpServletRequest aRequest, Model aModel,
	    @ModelAttribute(value = "aAcctAdjustApply") AcctAdjustApply aAcctAdjustApply, UserToken aUserToken) {
	  User oUser = (User) aRequest.getSession().getAttribute("user");
	  aUserToken = oUser.getUserToken();
	  CMReturn oRtn = aAcctChangeApplyService.submit(aAcctAdjustApply.getRow_id(), aUserToken);
	  if (oRtn.getValue() < 0) {
	    List<AcctChangeType> changeType = aAcctChangeApplyService.queryAcctChangeType();
	    aModel.addAttribute("changeTypes", changeType);
	    List<Bank> Banks=aAcctChangeApplyService.getBank(aUserToken);
	    aModel.addAttribute("banks", Banks);
	    List<HouseBank> province = aAcctChangeApplyService.queryProvince();
	    aModel.addAttribute("province", province);
	    List<Currency> Currencys=aAcctChangeApplyService.getCurrency(aUserToken);
	    aModel.addAttribute("currencys", Currencys);
	    aModel.addAttribute("oRtn", oRtn);
	    return "eca/am/acct/change/apply/AcctAdjustApplyAddEdit";
	  }
	  return "redirect:list?originFlag=1";
	}
	@RequestMapping("submitOneCancel")
	public String submitOneCancel(HttpServletRequest aRequest, Model aModel,
	    @ModelAttribute(value = "aAcctCancelApply") AcctCancelApply aAcctCancelApply, UserToken aUserToken) {
	  User oUser = (User) aRequest.getSession().getAttribute("user");
	  aUserToken = oUser.getUserToken();
	  CMReturn oRtn = aAcctChangeApplyService.submit(aAcctCancelApply.getRow_id(), aUserToken);
	  if (oRtn.getValue() < 0) {
	    List<AcctChangeType> changeType = aAcctChangeApplyService.queryAcctChangeType();
	    aModel.addAttribute("changeTypes", changeType);
	    List<Bank> Banks=aAcctChangeApplyService.getBank(aUserToken);
	    aModel.addAttribute("banks", Banks);
	    List<HouseBank> province = aAcctChangeApplyService.queryProvince();
	    aModel.addAttribute("province", province);
	    List<Currency> Currencys=aAcctChangeApplyService.getCurrency(aUserToken);
	    aModel.addAttribute("currencys", Currencys);
	    aModel.addAttribute("oRtn", oRtn);
	    return "eca/am/acct/change/apply/AcctCancelApplyAddEdit";
	  }
	  return "redirect:list?originFlag=1";
	}

	@RequestMapping("returnToList")
	public String returnToList() {
		return "redirect:list?originFlag=1";
	}

	@RequestMapping(value = "export", method = RequestMethod.POST)
	public void export(HttpServletResponse response, HttpServletRequest aRequest, HttpSession aSession,
			String[] titleField, String[] titleValue, String[] titleSize, String originFlag, Integer aPageNo,
			UserToken aUserToken, AcctChangeApplySearchCond aCond) throws Exception {
		String sMsg = Interlization.getMessage(aUserToken.getLang_type(), "eca.am.acct.change.apply.export.info");
		String formatDate = DateUtils.formatDate(DateUtils.getSysTime(), "yyyyMMddHHmmss");
    XlsxExport xlsExp = new XlsxExport(aRequest,response,sMsg+formatDate, "sheet1");
		ArrayList<ColDefEntity> colDefs = new ArrayList<ColDefEntity>();
		Map<Integer, String> dateformat = new HashMap<>();
    dateformat.put(7, "datetime");
		for (int i = 0; i < titleField.length; i++) {
			colDefs.add(new ColDefEntity(titleField[i], titleValue[i], Integer.parseInt(titleSize[i]), dateformat));
		}
		xlsExp.setDataSource(new ExcelDataSource() {
			User oUser = (User) aRequest.getSession().getAttribute("user");
			UserToken aUserToken = oUser.getUserToken();

			@Override
			public List<?> queryPage(int aPageNo) {
				Integer PageNo = aPageNo == 0 ? 1 : aPageNo;
				List<AcctChangeApply> datas = null;
				try {
					Page<AcctChangeApply> oRtn = aAcctChangeApplyService.queryPage(aCond, PageNo, aUserToken);
					datas = oRtn.getDatas();
				} catch (Exception e) {
					logger.error("",e);
				}
				return datas;
			}

			@Override
			public int getPageCnt() {
				Page<AcctChangeApply> oRtn = aAcctChangeApplyService.queryPage(aCond, 1, aUserToken);
				int pageCnt = oRtn.getRowCnt();
				return pageCnt;
			}

		});
		xlsExp.setRowColPosition(0, 0);
		xlsExp.setTitleAlignment(HorizontalAlignment.CENTER);
		xlsExp.wirteExcel(colDefs);
	}

	@RequestMapping("delete")
  @ResponseBody
  public CMReturn delete(HttpServletRequest aRequest,long[] aIds,UserToken aUserToken) {
    User oUser = (User)aRequest.getSession().getAttribute("user");
    aUserToken = oUser.getUserToken();
    CMReturn oRtn = aAcctChangeApplyService.delete(aIds, aUserToken);
    if(oRtn.getMsgCode()!=null&&!"".equalsIgnoreCase(oRtn.getMsgCode())) {
      String sMsg = Interlization.getMessage(aUserToken.getLang_type(), oRtn.getMsgCode());
      Object[] params = {oRtn.getReturnPara("0", ""),oRtn.getReturnPara("1", "")};
      MessageFormat mf = new MessageFormat(sMsg);
      String sContent = mf.format(params);
      oRtn.setValue(oRtn.getValue(), sContent);
    }
    return oRtn;
  }
	@RequestMapping(value = "fileUpload", method = RequestMethod.POST)
	@ResponseBody
	public CMReturn fileUpload(HttpServletRequest aRequest, Model aModel,
			@RequestParam(value = "file") MultipartFile file, UserToken aUserToken) {
		User aUser = (User) aRequest.getSession().getAttribute("user");
		aUserToken = aUser.getUserToken();
		Excel<AcctChangeApply> readExcel = null;
		// 从第几行开始查询
		int rowNum = 1;
		if (!StringUtils.isEmpty(file)) {
			ExcelImport excelImport = new ExcelImport(rowNum);
			readExcel = excelImport.readExcel(file, AcctChangeApply.class);
			if (readExcel.getValue() > 0) {
				List<AcctChangeApply> excelBeans = readExcel.getDatas();
				CMReturn oRtn = new CMReturn(1);
				if (excelBeans != null) {
					oRtn = aAcctChangeApplyService.applyImport(excelBeans, aUserToken, rowNum);
					if (oRtn.getMsgCode() != null && !"".equalsIgnoreCase(oRtn.getMsgCode())) {
						String sMsg = Interlization.getMessage(aUserToken.getLang_type(), oRtn.getMsgCode());
						Object[] params = { oRtn.getReturnPara("0", "") };
						MessageFormat mf = new MessageFormat(sMsg);
						String sContent = mf.format(params);
						oRtn.setValueCode(oRtn.getValue(), sContent);
					}
				}
			} else {
				if (readExcel.getMsgCode() != null && !"".equalsIgnoreCase(readExcel.getMsgCode())) {
					String sMsg = Interlization.getMessage(aUserToken.getLang_type(), readExcel.getMsgCode());
					Object[] params = { readExcel.getReturnPara("0", ""), readExcel.getReturnPara("1", "") };
					MessageFormat mf = new MessageFormat(sMsg);
					String sContent = mf.format(params);
					return new CMReturn(1).setValueCode(readExcel.getValue(), sContent);
				}
			}

			if (readExcel.getValue() == -1) {
				return new CMReturn(1).setValueCode(readExcel.getValue(),
						Interlization.getMessage(aUserToken.getLang_type(), readExcel.getMsgCode()));
			} else {
				return new CMReturn(1).setValueCode(1, Interlization.getMessage(aUserToken.getLang_type(),
						"eca.system.common.file.importsuccess"));
			}
		}
		return new CMReturn(1).setValueCode(-1,
				Interlization.getMessage(aUserToken.getLang_type(), "eca.system.common.file.importnullfile"));
	}
	/**
   * 将一个对象转换为另一个对象需要两个bean对象的变量相同
   *
   * @param <T1>      要转换的对象
   * @param <T2>      转换后的类
   * @param orimodel  要转换的对象
   * @param castClass 转换后的类
   * @return 转换后的对象
   */
  @SuppressWarnings("unchecked")
  public static <T1, T2> T2 convertBean(T1 orimodel, Class<T2> castClass) {
      T2 returnModel;
      try {
          returnModel = castClass.newInstance();
      } catch (Exception e) {
    	  logger.error("", e);
          throw new RuntimeException("创建" + castClass.getName() + "对象失败");
      }
      //要转换的字段集合
      List<Field> fieldlist = new ArrayList<Field>();
      final String objClass = "java.lang.object";
      //循环获取要转换的字段,包括父类的字段
      while (castClass != null && !objClass.equals(castClass.getName().toLowerCase())) {
          fieldlist.addAll(Arrays.asList(castClass.getDeclaredFields()));
          //得到父类,然后赋给自己
          castClass = (Class<T2>) castClass.getSuperclass();
      }
      for (Field field : fieldlist) {
          PropertyDescriptor getpd;
          PropertyDescriptor setpd;
          try {
              getpd = new PropertyDescriptor(field.getName(), orimodel.getClass());
              setpd = new PropertyDescriptor(field.getName(), returnModel.getClass());
          } catch (Exception e) {
        	  logger.error("", e);
              continue;
          }
          try {
              Method getMethod = getpd.getReadMethod();
              Object transValue = getMethod.invoke(orimodel);
              Method setMethod = setpd.getWriteMethod();
              setMethod.invoke(returnModel, transValue);
          } catch (Exception e) {
        	  logger.error("", e);
              throw new RuntimeException("cast " + orimodel.getClass().getName() + "to "
                      + castClass.getName() + " failed");
          }
      }
      return returnModel;
  }
}