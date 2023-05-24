// PRD_0123 #7054 add JFE start
package jp.co.jun.edi.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.entity.MCodmstEntity2;
import jp.co.jun.edi.model.DeliveryLocationModel;
import jp.co.jun.edi.repository.MCodmstEntityRepository;
import jp.co.jun.edi.repository.MDeliverylocationRepository;
import jp.co.jun.edi.service.parameter.GetServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;


/**
 * 読み込まれた物流コードを基に納入場所リストを取得するサービス.
 */
@Service
public class MDeliveryLocationGetService
extends GenericListService<GetServiceParameter<BigInteger>, GetServiceResponse<List<DeliveryLocationModel>>> {

	public static final String M_CODMST_ENTITY_2_CLASS = "jp.co.jun.edi.entity.MCodmstEntity2";
	public static final String METHOD_NAME = "getItem";

	@Autowired
	private MCodmstEntityRepository mCodmstEntityRepository;
	@Autowired
	private MDeliverylocationRepository mDeliverylocationRepository;

	@Override
	protected GetServiceResponse<List<DeliveryLocationModel>> execute(final	GetServiceParameter<BigInteger> serviceParameter) {
		// 物流コードに紐づく納入場所リスト(検索結果)
		List<DeliveryLocationModel> deliveryLocationList = new ArrayList<>();

		final MCodmstEntity2 mce =  mCodmstEntityRepository.selectOneByCode1(serviceParameter.getId());
		if(mce != null) {
		List<String> itemList = getItem(mce);
		for(String logisticsCode : itemList) {
			String companyName = mDeliverylocationRepository.selectCompanyNameByLogisticsCode(logisticsCode);
			deliveryLocationList.add(new DeliveryLocationModel(logisticsCode, companyName));
		}
		}
        return GetServiceResponse.<List<DeliveryLocationModel>>builder().item(deliveryLocationList).build();
	}

	private List<String> getItem(MCodmstEntity2 mce){
		List<String> itemList = new ArrayList<String>();
		try {
			Class<?> clazz = Class.forName(M_CODMST_ENTITY_2_CLASS);
			Object classInstance = clazz.newInstance();
			for(int i = 1; i < 31; i++) {
				Method method =  classInstance.getClass().getDeclaredMethod(METHOD_NAME+i);
				String logisticsCode = method.invoke(mce)+"";
				if("null".equals(logisticsCode)||"".equals(logisticsCode))continue;
				itemList.add(logisticsCode);
			}

        } catch (ClassNotFoundException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return itemList;
	}

}
// PRD_0123 #7054 add JFE end