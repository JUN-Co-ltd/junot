package jp.co.jun.edi.component.schedule;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.component.MKanmstComponent;
import jp.co.jun.edi.component.model.TagdatModel;
import jp.co.jun.edi.entity.MKanmstEntity;
import jp.co.jun.edi.entity.TagdatEntity;
import jp.co.jun.edi.entity.extended.ExtendedTagdatEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.TItemRepository;
import jp.co.jun.edi.repository.TOrderRepository;
import jp.co.jun.edi.repository.TagdatRepository;
import jp.co.jun.edi.repository.extended.ExtendedTagdatRepository;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.LogStringUtil;
import jp.co.jun.edi.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * TAGDAT作成スケジュールのコンポーネント.
 */
@Component
@Slf4j
public class TagdatCreateScheduleComponent {

    // TagdatModelにセットする定数
    /** 空文字. */
    private static final String EMPTY = "";
    /** ゼロ2桁. */
    private static final String STRING_TWO_ZERO = "00";
    /** ゼロ4桁. */
    private static final String STRING_FOUR_ZERO = "0000";
    /** ゼロ9桁. */
    private static final String STRING_NINE_ZERO = "000000000";
    /** ゼロ12桁. */
    private static final String STRING_TWELVE_ZERO = "000000000000";

    // 文字切り捨て・拡張桁数
    /** 5桁. */
    private static final int DIGIT_5 = 5;
    /** 6桁. */
    private static final int DIGIT_6 = 6;
    /** 8桁. */
    private static final int DIGIT_8 = 8;
    /** 9桁. */
    private static final int DIGIT_9 = 9;

    /** 発注データSEQ初期値. */
    private static final int ORDER_SEQ = 00001;
    /** 品番データSEQ初期値. */
    private static final int ITEM_SEQ = 60001;
    /** 送信ステータス：未送信. */
    private static final int SEND_STATUS = 0;
    /** 軽減税率. */
    private static final double REDUCE_TAX = 1.08;

    @Autowired
    private TOrderRepository tOrderRepository;

    @Autowired
    private TItemRepository tItemRepository;

    @Autowired
    private ExtendedTagdatRepository extendedTagdatRepository;

    @Autowired
    private TagdatRepository tagdatRepository;

    @Autowired
    private MKanmstComponent kanmstComponent;


    /**
     * TAGDAT作成実行.
     *
     * @param userId システムユーザーID
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void execute(final BigInteger userId) {
        try {

        	// TAGDAT登録用品番データリスト取得
            final List<ExtendedTagdatEntity> itemEntity = extendedTagdatRepository.findItem(PageRequest.of(0, Integer.MAX_VALUE)).getContent();
            // TAGDAT登録用発注データリスト取得
            final List<ExtendedTagdatEntity> orderEntity = extendedTagdatRepository.findOrder(PageRequest.of(0, Integer.MAX_VALUE)).getContent();

            if (!CollectionUtils.isEmpty(itemEntity)) {
            	// TAGDAT登録用品番データリストを基に、TAGDAT登録用Modelリストを作成
                final List<TagdatModel> itemModels = generateInfo(itemEntity, ITEM_SEQ);

                // TAGDAT登録用ModelをTagdatEntityへコピー
                for(final TagdatModel tagdat: itemModels) {
                	final TagdatEntity tagdatEntity = new TagdatEntity();
                	copyModelToTagdatEntity(tagdat, tagdatEntity);
                	// 送信ステータス
                	tagdatEntity.setSendStatus(SEND_STATUS);
                	// 登録ユーザID
                	tagdatEntity.setCreatedUserId(userId);
                	// 更新ユーザID
                	tagdatEntity.setUpdatedUserId(userId);
                	// TAGDAT登録
                    tagdatSave(tagdatEntity);
                }

                // 品番情報の更新対象idリスト取得
                final List<BigInteger> items = tItemRepository.findItem();
                // 品番情報のTAGDAT作成フラグ更新
                for (final BigInteger id : items) {
            		tItemRepository.updateTagdatCreatedFlg(id, userId);
            	}
            }

            if (!CollectionUtils.isEmpty(orderEntity)) {
            	// TAGDAT登録用発注データリストを基に、TAGDAT登録用Modelリストを作成
                final List<TagdatModel> orderModels = generateInfo(orderEntity, ORDER_SEQ);

                // TAGDAT登録用ModelをTagdatEntityへコピー
                for(final TagdatModel tagdat: orderModels) {
                	final TagdatEntity tagdatEntity = new TagdatEntity();
                	copyModelToTagdatEntity(tagdat, tagdatEntity);
                	// 送信ステータス
                	tagdatEntity.setSendStatus(SEND_STATUS);
                	// 登録ユーザID
                	tagdatEntity.setCreatedUserId(userId);
                	// 更新ユーザID
                	tagdatEntity.setUpdatedUserId(userId);
                	// TAGDAT登録
                    tagdatSave(tagdatEntity);
                }

                // 発注情報の更新対象idリスト取得
                final List<BigInteger> orders = tOrderRepository.findOrder();
                // 発注情報のTAGDAT作成フラグ更新
                for (final BigInteger id : orders) {
            		tOrderRepository.updateTagdatCreatedFlg(id, userId);
            	}
            }

        } catch (RuntimeException e) {
        	throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * TAGDAT作成用Modelを取得する.
     *
     * @param tagdats TAGDAT作成用データリスト
     * @return TAGDAT作成用用Modelリスト
     */
    private List<TagdatModel> generateInfo(
            final List<ExtendedTagdatEntity> tagdats, int seq) {
        // TAGDAT作成用品番Modelリスト作成
        final List<TagdatModel> tagdatModels = tagdats
                .stream()
                .map(tagdat -> generateTagdatModel(tagdat))
                .collect(Collectors.toList());

        // 採番項目のセット
        generateNumberingItem(tagdatModels, seq);

        return tagdatModels;
    }

    /**
     * 採番項目データセット.
     * ・SEQ
     *
     * @param datModels ファイル作成用Modelリスト
     */
    private void generateNumberingItem(final List<TagdatModel> tagdatModels, int seq) {
        // 採番順にソート
        sortForNumbering(tagdatModels);

        // 採番用Map作成
        final Map<String, List<TagdatModel>> datMap = groupingForNumbering(tagdatModels);

        // 採番項目セット：
        for (final Entry<String, List<TagdatModel>> entry : datMap.entrySet()) {
        	int num = seq;
            for (final TagdatModel dat : entry.getValue()) {
                // SEQ
                dat.setSeq(StringUtils.toStringPadding0(num, DIGIT_5));

                num++;
            }
        }
    }

    /**
     * 採番用にブランドでグルーピング.
     *
     * @param datModels ファイル作成用Modelリスト
     * @return グルーピング後のMap
     */
    private Map<String, List<TagdatModel>> groupingForNumbering(
            final List<TagdatModel> tagdatModels) {
        // グルーピング
        return tagdatModels.stream().collect(
                Collectors.groupingBy(dat -> dat.getBrkg(),
                        // ※順番変えない
                        LinkedHashMap::new,
                        Collectors.toList()));
    }

    /**
     * 採番順にソート.
     * (1) ブランド,昇順
     * (2) 作成日,昇順
     *
     * @param tagdatModels TAGDAT作成用Modelリスト
     */
    private void sortForNumbering(final List<TagdatModel> tagdatModels) {
        Collections.sort(tagdatModels,
                Comparator.comparing(TagdatModel::getBrkg)
                .thenComparing(TagdatModel::getCrtymd));
    }

    /**
     * DATファイル作成用Modelを作成する.
     *
     * @param tagdat TAGDAT作成用Entity
     * @return DATファイル作成用Model
     */
    private TagdatModel generateTagdatModel(
            final ExtendedTagdatEntity tagdat) {
        final TagdatModel tagdatModel = new TagdatModel();

        // ExtendedTagdatEntityを基にデータセット
        generateDataByEntity(tagdatModel, tagdat);
        // 固定値データセット
        generateFixedData(tagdatModel);

        return tagdatModel;
    }

    /**
     * ExtendedTagdatEntityを基にデータセット.
     *
     * @param tagdatModel TAGDAT作成用Model
     * @param tagdat TAGDAT登録用データ
     */
    private void generateDataByEntity(
            final TagdatModel tagdatModel,
            final ExtendedTagdatEntity tagdat) {
        // 作成日
    	tagdatModel.setCrtymd(tagdat.getCrtymd());

    	// ブランド  ※NULLの場合は空文字
    	if (Objects.isNull(tagdat.getBrkg())) {
        	tagdatModel.setBrkg(EMPTY);
        } else {
        	tagdatModel.setBrkg(tagdat.getBrkg());
        }

        // 年度 ※NULLの場合は"0000"固定
        if (Objects.isNull(tagdat.getDatrec())) {
        	tagdatModel.setDatrec(STRING_FOUR_ZERO);
        } else {
        	tagdatModel.setDatrec(tagdat.getDatrec().toString());
        }

        // シーズン ※NULLの場合は空文字
        if (Objects.isNull(tagdat.getSeason())) {
        	tagdatModel.setSeason(EMPTY);
        } else {
        	tagdatModel.setSeason(tagdat.getSeason());
        }
        // 品番
        if (Objects.isNull(tagdat.getPartNo())) {
        	// 品番・品種 ※NULLの場合は空文字
        	tagdatModel.setHskg(EMPTY);
        	// 品番・通番 ※NULLの場合は空文字
        	tagdatModel.setTuban(EMPTY);
        } else {
        	// 品番・品種
        	tagdatModel.setHskg(StringUtils.paddingSpaceRight(tagdat.getPartNo(), DIGIT_8).substring(0,3));
        	// 品番・通番
        	tagdatModel.setTuban(StringUtils.paddingSpaceRight(tagdat.getPartNo(), DIGIT_8).substring(3));
        }
        // 上代 ※NULLの場合は"000000000"固定
        if (Objects.isNull(tagdat.getJodai())) {
        	tagdatModel.setJodai(STRING_NINE_ZERO);
        } else {
        	tagdatModel.setJodai(StringUtils.toStringPadding0(tagdat.getJodai().intValue(), DIGIT_9));
        }
        // 発注番号
        tagdatModel.setHacno(StringUtils.toStringPadding0(tagdat.getHacno(), DIGIT_6));
        // カラー
        tagdatModel.setIro(tagdat.getIro());
        // サイズ記号
        tagdatModel.setSzkg(tagdat.getSzkg());
        // JAN ※NULLの場合は空文字
        if (Objects.isNull(tagdat.getJan())) {
        	tagdatModel.setJan(EMPTY);
        } else {
        	tagdatModel.setJan(tagdat.getJan());
        }
        // 税込上代
        tagdatModel.setZjodai(generateZjodai(tagdat.getJodai(), tagdat.getTaxflg()));
        // 作成時間
        tagdatModel.setCrthms(tagdat.getCrthms());
        // 送信種別
        tagdatModel.setSyubt(tagdat.getSyubt());
    }

    /**
     * 「税込上代」の設定.
     *
     * @param jodai 上代
     * @param flg 軽減税率対象フラグ
     * @return 税込上代
     */
    private String generateZjodai(
            final BigDecimal jodai,
            final Integer flg) {
    	// 管理マスタ取得
        final MKanmstEntity kanmstEntity = kanmstComponent.getMKanmstEntity();
        // 改正後税率
        final BigDecimal tax2 = BigDecimal.valueOf(kanmstEntity.getTax2() / 100 + 1.0);
        // 軽減税率
        final BigDecimal tax = BigDecimal.valueOf(REDUCE_TAX);
        // 上代がNULLでない
        if (jodai != null) {
        	// 軽減税率対象
        	if (flg == 1) {
        		final BigDecimal reduceZjodai = jodai.multiply(tax).setScale(0, RoundingMode.UP);
        		checkOverLength("zjodai", String.valueOf(reduceZjodai.intValue()), DIGIT_9);
        		return StringUtils.toStringPadding0(reduceZjodai.intValue(), DIGIT_9);
        	} else {
        		final BigDecimal zjodai = jodai.multiply(tax2).setScale(0, RoundingMode.UP);
        		checkOverLength("zjodai", String.valueOf(zjodai.intValue()), DIGIT_9);
        		return StringUtils.toStringPadding0(zjodai.intValue(), DIGIT_9);
        	}
        // 上代がNULLの場合
        } else {
        	return StringUtils.toStringPadding0(BigInteger.ZERO, DIGIT_9);
        }
    }

    /**
     * 固定値データセット.
     *
     * @param tagdatModel TAGDAT作成用Model
     */
    private void generateFixedData(final TagdatModel tagdatModel) {
        // 引取回数
    	tagdatModel.setNkai(STRING_TWO_ZERO);
        // サイズコード
    	tagdatModel.setSize(STRING_TWO_ZERO);
        // NW7
    	tagdatModel.setNw7(STRING_TWELVE_ZERO);
        // 予備 空文字
    	tagdatModel.setYobi(EMPTY);
    }

    /**
     * 桁数超過チェック.
     * 指定桁数を超過した場合はエラー
     *
     * @param item 項目名
     * @param value チェック対象文字列
     * @param length 指定桁数
     */
    private void checkOverLength(
            final String item,
            final String value,
            final int length) {
        if (value.length() > length) {
            // 指定桁数以上の場合、桁数超過エラー
            throw new BusinessException(ResultMessages.warning().add(
                    MessageCodeType.SYSTEM_ERROR, LogStringUtil.of("generateInfo")
                    .message(item + " length over.")
                    .build()));
        }
    }

    /**
     * TagdatModel を TagdatEntity にコピーする.
     * @param model TagdatModel
     * @return entity TagdatEntity
     */
    public void copyModelToTagdatEntity(final TagdatModel model, final TagdatEntity entity) {
        entity.setCrtymd(model.getCrtymd());
        entity.setBrkg(model.getBrkg());
        entity.setSeq(model.getSeq());
        entity.setDatrec(model.getDatrec());
        entity.setSeason(model.getSeason());
        entity.setHskg(model.getHskg());
        entity.setTuban(model.getTuban());
        entity.setJodai(model.getJodai());
        entity.setHacno(model.getHacno());
        entity.setNkai(model.getNkai());
        entity.setIro(model.getIro());
        entity.setSize(model.getSize());
        entity.setSzkg(model.getSzkg());
        entity.setNw7(model.getNw7());
        entity.setJan(model.getJan());
        entity.setZjodai(model.getZjodai());
        entity.setYobi(model.getYobi());
        entity.setCrthms(model.getCrthms());
        entity.setSyubt(model.getSyubt());
    }

    /**
     * TAGDAT情報を保存する.
     *
     * @param entity {@link tagdatEntity} instance
     * @return {@link tagdatEntity} instance
     */
    public TagdatEntity tagdatSave(final TagdatEntity entity) {

        return tagdatRepository.save(entity);
    }

}
