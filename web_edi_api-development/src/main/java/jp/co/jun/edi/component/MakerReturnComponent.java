package jp.co.jun.edi.component;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.MakerReturnCompositeEntity;
import jp.co.jun.edi.entity.TMakerReturnEntity;
import jp.co.jun.edi.entity.TShopStockEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessage;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.MakerReturnModel;
import jp.co.jun.edi.model.MakerReturnProductCompositeModel;
import jp.co.jun.edi.repository.TShopStockRepository;
import jp.co.jun.edi.type.LgSendType;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.CollectionUtils;
import jp.co.jun.edi.util.DateUtils;

/**
 * メーカー返品関連のコンポーネント.
 */
@Component
public class MakerReturnComponent extends GenericComponent {

    @Autowired
    private TShopStockRepository shopStockRepository;

    @Autowired
    private MessageSource messageSource;

    private static final int OVER_DATE = 30;

    /**
     * @param resultList 取得結果リスト
     * @return レスポンス用のメーカーの返品モデル
     */
    public MakerReturnModel toResponseModel(final List<TMakerReturnEntity> resultList) {
        if (resultList.isEmpty()) {
            return null;
        }

        final MakerReturnModel res = new MakerReturnModel();

        // 共通項目コピー
        BeanUtils.copyProperties(resultList.get(0), res);

        // メーカー返品商品情報
        final List<MakerReturnProductCompositeModel> makerReturnProducts = resultList
                .stream()
                .map(this::toMakerReturnProductCompositeModel)
                .collect(Collectors.toList());
        res.setMakerReturnProducts(makerReturnProducts);

        return res;
    }

    /**
     * @param entity 処理中のメーカー返品情報
     * @return MakerReturnProductCompositeModel
     */
    private MakerReturnProductCompositeModel toMakerReturnProductCompositeModel(final TMakerReturnEntity entity) {
        final MakerReturnProductCompositeModel model = new MakerReturnProductCompositeModel();

        // メーカー返品商品情報リストコピー
        BeanUtils.copyProperties(entity, model);

        return model;
    }

    /**
     * 登録・更新共通のバリデーションチェックを行う.
     * @param request リクエスト値
     * @param rsltMsg エラーメッセージ
     */
    public void checkValidateAtUpsert(
            final MakerReturnModel request,
            final ResultMessages rsltMsg) {
        final List<TShopStockEntity> tShopStockEntities = shopStockRepository.findByShopCode(request.getDistaCode())
                .orElseThrow(() -> new ResourceNotFoundException(rsltMsg.add(MessageCodeType.CODE_002)));

        // 返品日が当日から前後30日を超えている場合、エラー
        final Date today = DateUtils.truncateDate(new Date());
        final Date maxDay = DateUtils.plusDays(today, OVER_DATE);
        final Date minDay = DateUtils.minusDays(today, OVER_DATE);
        final Date returnAt = request.getReturnAt();

        if (returnAt.before(minDay) || returnAt.after(maxDay)) {
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_MR_001));
        }

        // 返品数が在庫数を超えている場合、エラー
        request.getMakerReturnProducts().stream().forEach(makerReturnProduct -> {
            final Optional<TShopStockEntity> opt = tShopStockEntities.stream()
                    .filter(tShopStockEntity -> matchProductCode(tShopStockEntity, makerReturnProduct))
                    .findFirst();
            opt.ifPresent(tShopStockEntity -> {
                if (isOverStockLot(tShopStockEntity, makerReturnProduct)) {
                    // toString()しないと3桁区切りになる
                    rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_MR_003,
                            getMessage("code.400_MR_03", tShopStockEntity.getProductCode(), makerReturnProduct.getOrderNumber().toString())));
                }
            });
            if (!opt.isPresent()) {
                rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_MR_003,
                        getMessage("code.400_MR_03", generateProductCode(makerReturnProduct), makerReturnProduct.getOrderNumber().toString())));
            }
        });

        // 重複がある場合、エラー
        final long filteredLen =  request.getMakerReturnProducts().stream()
                .filter(CollectionUtils.distinctByKey(p -> p.getPartNo() + p.getColorCode() + p.getSize()))
                .count();
        if (filteredLen != request.getMakerReturnProducts().size()) {
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_MR_004));
        }
    }

    /**
     * @param tShopStockEntity TShopStockEntity
     * @param makerReturnProduct MakerReturnProduct
     * @return true:合致
     */
    private boolean matchProductCode(final TShopStockEntity tShopStockEntity, final MakerReturnProductCompositeModel makerReturnProduct) {
        return tShopStockEntity.getPartNo().equals(makerReturnProduct.getPartNo())
                && tShopStockEntity.getColorCode().equals(makerReturnProduct.getColorCode())
                && tShopStockEntity.getSize().equals(makerReturnProduct.getSize());
    }

    /**
     * @param tShopStockEntity TShopStockEntity
     * @param makerReturnProduct MakerReturnProduct
     * @return true:在庫数超過
     */
    private boolean isOverStockLot(final TShopStockEntity tShopStockEntity, final MakerReturnProductCompositeModel makerReturnProduct) {
        return tShopStockEntity.getStockLot() < makerReturnProduct.getReturnLot();
    }

    /**
     * @param makerReturnProduct MakerReturnProduct
     * @return 商品コード
     */
    private String generateProductCode(final MakerReturnProductCompositeModel makerReturnProduct) {
        return makerReturnProduct.getPartNo() + "-" + makerReturnProduct.getColorCode() + "-" + makerReturnProduct.getSize();
    }

    /**
     * 更新・削除共通のバリデーションチェックを行う.
     * @param dbMakerReturns DB登録済みのメーカー返品情報
     * @param rsltMsg エラーメッセージ
     */
    public void checkValidateAtUpdate(
            final List<TMakerReturnEntity> dbMakerReturns,
            final ResultMessages rsltMsg) {
        final boolean existsInstruction = dbMakerReturns.stream().anyMatch(db -> LgSendType.INSTRUCTION == db.getLgSendType());
        if (existsInstruction) {
            // LG送信指示の場合、エラー
            rsltMsg.add(ResultMessage.fromCode(MessageCodeType.CODE_MR_002));
        }
    }

    /**
     * @param code コード
     * @param args 引数
     * @return メッセージ
     */
    private String getMessage(final String code, final Object... args) {
        return messageSource.getMessage(code, args, Locale.JAPANESE);
    }

    /**
     * メーカー返品一覧リストを取得する.
     * @param pageTMakerReturn メーカー返品一覧検索結果
     * @return メーカ返品一覧検索結果Modelリスト
     */
    public List<MakerReturnModel> listMakerReturnSearchResult(final Page<MakerReturnCompositeEntity> pageTMakerReturn) {
        return pageTMakerReturn.stream().map(this::toModel).collect(Collectors.toList());
    }

    /**
     * EntityからModelへデータの詰め替えを行う.
     * @param makerReturnResultListEntity MakerReturnCompositeEntity
     * @return makerReturnModel
     */
    private MakerReturnModel toModel(final MakerReturnCompositeEntity makerReturnResultListEntity) {
        final MakerReturnModel makerReturnModel = new MakerReturnModel();

        BeanUtils.copyProperties(makerReturnResultListEntity, makerReturnModel);

        // キー項目をコピー
        makerReturnModel.setVoucherNumber(makerReturnResultListEntity.getKey().getVoucherNumber());
        makerReturnModel.setOrderId(makerReturnResultListEntity.getKey().getOrderId());

        return makerReturnModel;
    }

    // PRD_0073 add SIT start
    /**
     * 発注ID、伝票番号をキーに重複除去.
     * @param makerReturnModels メーカー返品情報リスト
     * @return 発注ID、伝票番号で重複除去したメーカー返品情報リスト
     */
    public List<MakerReturnModel> distinctMakerReturnByOrderIsAndVoucherNo(final List<MakerReturnModel> makerReturnModels) {
        return makerReturnModels.stream()
                .filter(jp.co.jun.edi.util.CollectionUtils.distinctByKey(model -> model.getOrderId() + model.getVoucherNumber()))
                .collect(Collectors.toList());
    }
    // PRD_0073 add SIT end
}
