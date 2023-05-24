package jp.co.jun.edi.component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.MCodmstEntity;
import jp.co.jun.edi.entity.MCodmstEntity2;
import jp.co.jun.edi.entity.TPosOrderDetailEntity;
import jp.co.jun.edi.entity.TShopStockEntity;
import jp.co.jun.edi.entity.extended.ExtendedMTnpmstEntity;
import jp.co.jun.edi.model.DeliveryLocationModel;
import jp.co.jun.edi.model.DeliveryStoreInfoModel;
import jp.co.jun.edi.model.JunpcStoreHrtmstModel;
import jp.co.jun.edi.model.JunpcTnpmstModel;
import jp.co.jun.edi.model.PosOrderDetailModel;
import jp.co.jun.edi.model.ScreenSettingDeliveryModel;
import jp.co.jun.edi.model.ScreenSettingDeliverySearchConditionModel;
import jp.co.jun.edi.model.ShopStockModel;
import jp.co.jun.edi.model.ThresholdModel;
import jp.co.jun.edi.repository.ExtendedMTnpmstRepository;
import jp.co.jun.edi.repository.MCodmstEntityRepository;
import jp.co.jun.edi.repository.MCodmstRepository;
import jp.co.jun.edi.repository.MDeliverylocationRepository;
import jp.co.jun.edi.repository.TPosOrderDetailRepository;
import jp.co.jun.edi.repository.TShopStockRepository;
import jp.co.jun.edi.type.MCodmstTblIdType;
import jp.co.jun.edi.type.ScreenSettingDeliveryMasterType;
import jp.co.jun.edi.util.DateUtils;

/**
 * 納品依頼用画面構成情報を取得するコンポーネント.
 */
@Component
public class ScreenSettingDeliveryComponent {
    @Autowired
    private ExtendedMTnpmstRepository mTnpmstRepository;

    @Autowired
    private MCodmstRepository mCodmstRepository;

    // PRD_0031 add SIT start
    @Autowired
    private TShopStockRepository shopStockRepository;
    // PRD_0031 add SIT end

    // PRD_0033 add SIT start
    @Autowired
    private TPosOrderDetailRepository posOrderDetailRepository;
    // PRD_0033 add SIT end

    @Autowired
    private ThresholdComponent thresholdComponent;

    @Autowired
    private JunpcStoreHrtmstComponent junpcStoreHrtmstComponent;

    // PRD_0123 #7054 add JFE start
	@Autowired
	private MCodmstEntityRepository mCodmstEntityRepository;

	@Autowired
	private MDeliverylocationRepository mDeliverylocationRepository;
	// PRD_0123 #7054 add JFE end

    private static final int CODE1_LENGTH = 4;
    // PRD_0123 #7054 add JFE start
    private static final String M_CODMST_ENTITY_2_CLASS = "jp.co.jun.edi.entity.MCodmstEntity2";
    private static final String METHOD_NAME = "getItem";
	// PRD_0123 #7054 add JFE end

    /**
     * 納品依頼関連の画面構成に必要な情報を取得する.
     * @param paramModel {@link ScreenSettingDeliverySearchConditionModel} instance
     * @return {@link ScreenSettingDeliveryModel} instance
     */
    public ScreenSettingDeliveryModel execute(final ScreenSettingDeliverySearchConditionModel paramModel) {

        final ScreenSettingDeliveryModel returnModel = new ScreenSettingDeliveryModel();

        for (ScreenSettingDeliveryMasterType masterType : paramModel.getListMasterType()) {
            switch (masterType) {
                // 閾値
                case THRESHOLD:
                    generateThreshold(returnModel, paramModel);
                break;
                // 店舗別配分率
                case STORE_HRTMST:
                    generateStoreHrtmst(returnModel, paramModel);
                    break;
                // 店舗マスタ
                case TNPMST:
                    generateTnpmst(returnModel, paramModel);
                    break;
                // PRD_0031 add SIT start
                // 在庫数
                case SHOP_STOCK:
                    generateShopStock(returnModel, paramModel);
                    break;
                // PRD_0031 add SIT end
                // PRD_0033 add SIT start
                // 売上数
                case POS_SALES_QUANTITY:
                    generatePosSalesQuantity(returnModel, paramModel);
                    break;
                // PRD_0033 add SIT end
				// PRD_0123 #7054 add JFE start
                case DELIVERY_LOCATION:
                	getDeliveryLocation(returnModel, paramModel);
                    break;
				// PRD_0123 #7054 add JFE end
            default:
                break;
            }
        }

        return returnModel;
    }

    /**
     * 店舗マスタから{@link List<TnpmstModel>} を生成し、{@link ScreenSettingDeliveryModel}に格納する.
     * @param returnModel {@link ScreenSettingDeliveryModel} instance
     * @param paramModel {@link ScreenSettingDeliverySearchConditionModel} instance
     */
    private void generateTnpmst(final ScreenSettingDeliveryModel returnModel,
            final ScreenSettingDeliverySearchConditionModel paramModel) {

        // 配分課マスタ
        final List<MCodmstEntity> mAllocationList = mCodmstRepository
                .findAllocationByTblidAndCode1AheadLikeOrderById(
                        MCodmstTblIdType.ALLOCATION.getValue(),
                        paramModel.getBrandCode(),
                        PageRequest.of(0, Integer.MAX_VALUE))
                .getContent();

        // 店舗マスタ
        final List<JunpcTnpmstModel> tnpmstList = mTnpmstRepository
                .findBySpec(paramModel)
                .stream()
                .map(mTnpEntity -> generateJunpcTnpmstModelFromEntity(mTnpEntity, mAllocationList, paramModel.getDeliveryStoreInfos()))
                .collect(Collectors.toList());

        // 1.課 昇順、2.配分順 昇順,null(m_tnpmstから削除されたもの)は課内の最後
        Collections.sort(tnpmstList, Comparator.comparing((JunpcTnpmstModel m) -> Integer.parseInt(m.getHka()))
                .thenComparingInt(m -> Integer.parseInt(m.getHjun())));

        returnModel.setTnpmstList(tnpmstList);
    }

    /**
     * 店舗マスタEntity・配分課マスタEntityから店舗マスタModelに設定する.
     * @param mTnpEntity 店舗マスタEntity
     * @param mAllocationList 配分課マスタEntityリスト
     * @param deliveryStoreInfos 店舗情報リスト
     * @return 店舗マスタModel
     */
    private JunpcTnpmstModel generateJunpcTnpmstModelFromEntity(final ExtendedMTnpmstEntity mTnpEntity,
            final List<MCodmstEntity> mAllocationList, final List<DeliveryStoreInfoModel> deliveryStoreInfos) {

        generateHkaIfNull(mTnpEntity, deliveryStoreInfos);
        final JunpcTnpmstModel junpcTnpmstModel = new JunpcTnpmstModel();
        BeanUtils.copyProperties(mTnpEntity, junpcTnpmstModel);

        final Optional<MCodmstEntity> optionalMCodmstEntity = mAllocationList.stream()
                .filter(mCodmst -> mCodmst.getCode1().substring(2, CODE1_LENGTH).equals(mTnpEntity.getHka()))
                .findFirst();

        if (optionalMCodmstEntity.isPresent()) {
            final String item3 = optionalMCodmstEntity.get().getItem3();
            junpcTnpmstModel.setLogisticsCode(item3.substring(0, 2));
            junpcTnpmstModel.setAllocationCode(item3.substring(0, 1));
        }

        return junpcTnpmstModel;
    }

    /**
     * 店舗マスタEntityの配分課がnullの場合にリクエストパラメータから設定する.
     * @param mTnpEntity 店舗マスタEntity
     * @param deliveryStoreInfos 店舗情報リスト(リクエストパラメータ)
     */
    private void generateHkaIfNull(final ExtendedMTnpmstEntity mTnpEntity, final List<DeliveryStoreInfoModel> deliveryStoreInfos) {
        if (mTnpEntity.getHka() != null) {
            return;
        }

        final Optional<DeliveryStoreInfoModel> opt = deliveryStoreInfos.stream()
                .filter(store -> mTnpEntity.getShpcd().equals(store.getStoreCode()))
                .findFirst();

        // 店舗マスタにもない店舗コードであれば設定なし
        if (!opt.isPresent()) {
            return;
        }

        mTnpEntity.setHka(opt.get().getDivisionCode());
        mTnpEntity.setHjun(String.valueOf(Integer.MAX_VALUE));
    }

    /**
     * 閾値マスタから{@link BigDecimal} を生成し、{@link ScreenSettingDeliveryModel}に格納する.
     * @param returnModel {@link ScreenSettingDeliveryModel} instance
     * @param paramModel {@link ScreenSettingDeliverySearchConditionModel} instance
     */
    private void generateThreshold(final ScreenSettingDeliveryModel returnModel,
            final ScreenSettingDeliverySearchConditionModel paramModel) {
        // 閾値取得
        final List<ThresholdModel> thresholdList = thresholdComponent.listThreshold(
                paramModel.getBrandCode(),
                paramModel.getItemCode());
        if (!Objects.isNull(thresholdList) && thresholdList.size() > 0) {
            returnModel.setThreshold(thresholdList.get(0).getThreshold());
        }
    }

    /**
     * 店舗別配分率マスタから{@link List<JunpcStoreHrtmstModel>} を生成し、{@link ScreenSettingDeliveryModel}に格納する.
     * @param returnModel {@link ScreenSettingDeliveryModel} instance
     * @param paramModel {@link ScreenSettingDeliverySearchConditionModel} instance
     */
    private void generateStoreHrtmst(final ScreenSettingDeliveryModel returnModel,
            final ScreenSettingDeliverySearchConditionModel paramModel) {
        final List<JunpcStoreHrtmstModel> storeHrtmstList = junpcStoreHrtmstComponent.findStoreHrtmst(
                paramModel.getBrandCode(),
                paramModel.getItemCode(),
                paramModel.getSeasonCode());
        returnModel.setStoreHrtmstList(storeHrtmstList);
    }

    // PRD_0031 add SIT start
    /**
     * 店別在庫情報から{@link List<ShopStock>} を生成し、{@link ScreenSettingDeliveryModel}に格納する.
     * @param returnModel {@link ScreenSettingDeliveryModel} instance
     * @param paramModel {@link ScreenSettingDeliverySearchConditionModel} instance
     */
    private void generateShopStock(final ScreenSettingDeliveryModel returnModel,
            final ScreenSettingDeliverySearchConditionModel paramModel) {

        final List<String> shpcds = returnModel.getTnpmstList()
                .stream().map(entity -> entity.getShpcd()).collect(Collectors.toList());

        final List<TShopStockEntity> shopStockEntities
                = shopStockRepository.findByPartNo(paramModel.getPartNo(), shpcds);

        returnModel.setShopStockList(shopStockEntities
                .stream()
                .map(shopStock ->{
                    final ShopStockModel shopStockModel = new ShopStockModel();

                    BeanUtils.copyProperties(shopStock, shopStockModel);

                    return shopStockModel;
                }).collect(Collectors.toList()));
    }
    // PRD_0031 add SIT end
    // PRD_0033 add SIT start
    /**
     * 売上情報明細から{@link List<PosOrderDetail>} を生成し、{@link ScreenSettingDeliveryModel}に格納する.
     * @param returnModel {@link ScreenSettingDeliveryModel} instance
     * @param paramModel {@link ScreenSettingDeliverySearchConditionModel} instance
     */
    private void generatePosSalesQuantity(final ScreenSettingDeliveryModel returnModel,
            final ScreenSettingDeliverySearchConditionModel paramModel) {

        // PRD_0094 mod SIT start
        //final Date currentDate = DateUtils.truncateDate(DateUtils.plusDays(new Date(), 1));
        final Date currentDate = DateUtils.truncateDate(new Date());
        // PRD_0094 mod SIT end
        final Date lastWeek = DateUtils.truncateDate(DateUtils.minusDays(new Date(), 7));

        final List<TPosOrderDetailEntity> posOrderDetailEntitys = posOrderDetailRepository.sumByPartNoDate(
                        paramModel.getPartNo(),
                        lastWeek,
                        currentDate);

        final List<PosOrderDetailModel> posOrderDetailModels = posOrderDetailEntitys.stream()
                .map(posOrderDetail ->{
                    final PosOrderDetailModel pod = new PosOrderDetailModel();
                    pod.setStoreCode(posOrderDetail.getStoreCode());
                    pod.setPartNo(posOrderDetail.getPartNo());
                    pod.setColorCode(posOrderDetail.getColorCode());
                    pod.setSizeCode(posOrderDetail.getSizeCode());
                    pod.setSalesScore(posOrderDetail.getSalesScore());
                    return pod;
                }).collect(Collectors.toList());

        returnModel.setPosOrderDetailList(posOrderDetailModels);
    }
    // PRD_0033 add SIT end

    // PRD_0123 #7054 add JFE start
    private void getDeliveryLocation (final ScreenSettingDeliveryModel returnModel,
            final ScreenSettingDeliverySearchConditionModel paramModel) {
    	List<DeliveryLocationModel> deliveryLocationList = new ArrayList<>();
    	final MCodmstEntity2 mce =  mCodmstEntityRepository.selectOneByCode1(paramModel.getId());
		if(mce != null) {
		List<String> itemList = getItem(mce);
		for(String logisticsCode : itemList) {
			String companyName = mDeliverylocationRepository.selectCompanyNameByLogisticsCode(logisticsCode);
			deliveryLocationList.add(new DeliveryLocationModel(logisticsCode, companyName));
		}
		}
		returnModel.setDeliveryLocationList(deliveryLocationList);
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
	// PRD_0123 #7054 add JFE end

}
