package jp.co.jun.edi.component.schedule;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jp.co.jun.edi.component.TagdatCreateCsvFileComponent;
import jp.co.jun.edi.component.TagdatMailCsvFileComponent;
import jp.co.jun.edi.component.model.TagdatModel;
import jp.co.jun.edi.entity.AdrmstEntity;
import jp.co.jun.edi.entity.TagdatEntity;
import jp.co.jun.edi.repository.AdrmstRepository;
import jp.co.jun.edi.repository.TagdatRepository;
import jp.co.jun.edi.util.LogStringUtil;
import jp.co.jun.edi.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * タグデータメール送信コンポーネント.
 */
@Slf4j
@Component
public class TagdatSendMailScheduleComponent {

	// DATファイルデータにセットする定数
    /** 半角スペース. */
    private static final String HALF_WIDTH_SPACE = " ";
    /** 2桁. */
    private static final int DIGIT_2 = 2;
    /** 3桁. */
    private static final int DIGIT_3 = 3;
    /** 4桁. */
    private static final int DIGIT_4 = 4;
    /** 5桁. */
    private static final int DIGIT_5 = 5;
    /** 13桁. */
    private static final int DIGIT_13 = 13;

	/** TAGDAT送信ステータス：送信済. */
    private static final int COMPLETED = 1;

    @Autowired
    private TagdatRepository tagdatRepository;

    @Autowired
    private AdrmstRepository adrmstRepository;

    @Autowired
    private TagdatCreateCsvFileComponent createCsvFileComponent;

    @Autowired
    private TagdatMailCsvFileComponent mailCsvFileComponent;

    /**
     *
     * タグデータメール送信実行.
     * @param listAdrmstEntity アドレスマスタ情報リスト
     * @param userId ユーザID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void execute(final List<TagdatEntity> listTagdatEntity, final BigInteger userId) {

    	 log.info(LogStringUtil.of("execute")
                 .message("Start processing of TagdatSendMailSchedule.")
                 .build());

        try {

            final List<File> createFiles = new ArrayList<File>();

            // ブランドでグルーピングする
            final Map<String, List<TagdatEntity>> mapTagdatEntity = listTagdatEntity
                    .stream()
                    .collect(Collectors.groupingBy(
                            entity -> entity.getBrkg()));

            // ブランドごとに、CSVファイル作成処理
            for(final Map.Entry<String, List<TagdatEntity>> map: mapTagdatEntity.entrySet()) {
            	if (!org.springframework.util.StringUtils.isEmpty(map.getKey())) {
				    	// タグデータ情報リストをデータ変換
				    	final List<TagdatEntity> convTagdatEntity = generateInfo(map.getValue());
				    	// CSVファイル生成
				        final File attachementFile = createCsvFileComponent.createCsvFile(userId, convTagdatEntity);
				        createFiles.add(attachementFile);
            	}
            }

            // アドレスマスタ情報取得
            final List<AdrmstEntity> listAdrmstEntity = adrmstRepository.findInfo(PageRequest.of(0, Integer.MAX_VALUE)).getContent();

            if (!CollectionUtils.isEmpty(listAdrmstEntity)) {
	            // メールアドレスでグルーピングする
	            final Map<String, List<AdrmstEntity>> mapAdrmstEntity = listAdrmstEntity
	                    .stream()
	                    .collect(Collectors.groupingBy(
	                            entity -> entity.getEmail()));

	            // メールアドレスでグルーピングしたリストごとに、メール送信処理
	            mapAdrmstEntity.entrySet().stream().forEach(data -> {
	            	mailCsvFileComponent.mailCsvFile(data.getValue(), userId);
	            });

                // 送信ステータスを「送信済(1)」に更新
	            for(final String brand: mapTagdatEntity.keySet()) {
	            	if (!org.springframework.util.StringUtils.isEmpty(brand)) {
	            		updateSendStatus(COMPLETED, brand, userId);
	            	}
	            }

	            // CSVファイルを削除
                createCsvFileComponent.deleteFiles(createFiles);
            }

                log.info(LogStringUtil.of("execute")
                        .message("End processing of TagdatSendMailSchedule.")
                        .build());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 処理対象のTAGDATメール送信情報のステータスを更新.
     * @param status 送信済(1)
     * @param brkg ブランドコード
     * @param userId ユーザID
     */
    private void updateSendStatus(final int status, final String brkg, final BigInteger userId) {
    	tagdatRepository.updateSendStatus(status, brkg, userId);
    }

    /**
     * TAGDAT作成用Modelを取得する.
     *
     * @param tagdats TAGDAT作成用データリスト
     * @return TAGDAT作成用用Modelリスト
     */
    private List<TagdatEntity> generateInfo(
            final List<TagdatEntity> tagdats) {
        // TAGDATモデルリスト作成
        final List<TagdatModel> tagdatModels = tagdats
                .stream()
                .map(entity -> generateDataByEntity(entity))
                .collect(Collectors.toList());

        // TAGDATエンティティリスト作成
        final List<TagdatEntity> tagdatEntities = tagdatModels
                .stream()
                .map(model -> copyModelToTagdatEntity(model))
                .collect(Collectors.toList());

        return tagdatEntities;
    }

    /**
     * TagdatEntityを基にデータセット.
     * @param entity TagdatEntity
     * @return model TagdatModel
     */
    private TagdatModel generateDataByEntity(
            final TagdatEntity entity) {
    	final TagdatModel model = new TagdatModel();

        // 作成日
    	model.setCrtymd(entity.getCrtymd());
    	// ブランド  ※空文字の場合は半角スペース2桁
    	if (org.springframework.util.StringUtils.isEmpty(entity.getBrkg())) {
    		model.setBrkg("  ");
        } else {
        	model.setBrkg(entity.getBrkg());
        }
    	// SEQ
    	model.setSeq(entity.getSeq());
        // 年度
    	model.setDatrec(entity.getDatrec());
        // シーズン ※空文字の場合は半角スペース1桁セット
        if (org.springframework.util.StringUtils.isEmpty(entity.getSeason())) {
        	model.setSeason(HALF_WIDTH_SPACE);
        } else {
        	model.setSeason(entity.getSeason());
        }
        // 品番・品種 ※空文字の場合は半角スペース3桁
        if (org.springframework.util.StringUtils.isEmpty(entity.getHskg())) {
        	model.setHskg("   ");
        } else {
        	model.setHskg(StringUtils.paddingSpaceRight(entity.getHskg(), DIGIT_3));
        }
        // 品番・通番 ※空文字の場合は半角スペース5桁
        if (org.springframework.util.StringUtils.isEmpty(entity.getTuban())) {
        	model.setTuban("     ");
        } else {
        	model.setTuban(StringUtils.paddingSpaceRight(entity.getTuban(), DIGIT_5));
        }
        // 上代
        model.setJodai(entity.getJodai());
        // 発注番号
        model.setHacno(entity.getHacno());
        // 引取回数
        model.setNkai(entity.getNkai());
        // カラー
        model.setIro(StringUtils.paddingSpaceRight(entity.getIro(), DIGIT_2));
        // サイズコード
        model.setSize(entity.getSize());
        // サイズ記号
        model.setSzkg(StringUtils.paddingSpaceRight(entity.getSzkg(), DIGIT_4));
        // NW7
        model.setNw7(entity.getNw7());
        // JAN ※空文字の場合は半角スペース13桁
        if (org.springframework.util.StringUtils.isEmpty(entity.getJan())) {
        	model.setJan("             ");
        } else {
        	model.setJan(StringUtils.paddingSpaceRight(entity.getJan(), DIGIT_13));
        }
        // 税込上代
        model.setZjodai(entity.getZjodai());
        // 予備 半角スペース19桁
        model.setYobi("                   ");
        // 作成時間
        model.setCrthms(entity.getCrthms());
        // 送信種別
        model.setSyubt(entity.getSyubt());

        return model;
    }

    /**
     * TagdatModel を TagdatEntity にコピーする.
     * @param model TagdatModel
     * @return entity TagdatEntity
     */
    private TagdatEntity copyModelToTagdatEntity(final TagdatModel model) {
    	final TagdatEntity entity = new TagdatEntity();

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

        return entity;
    }

}
