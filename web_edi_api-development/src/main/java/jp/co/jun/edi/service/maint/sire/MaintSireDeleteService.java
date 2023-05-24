package jp.co.jun.edi.service.maint.sire;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.maint.MaintSireComponent;
import jp.co.jun.edi.component.model.MaintSireReckbnKeyModel;
import jp.co.jun.edi.entity.MKojmstEntity;
import jp.co.jun.edi.entity.MSirmstEntity;
import jp.co.jun.edi.repository.MKojmstRepository;
import jp.co.jun.edi.repository.MSirmstRepository;
import jp.co.jun.edi.service.GenericDeleteService;
import jp.co.jun.edi.service.parameter.DeleteServiceParameter;
import jp.co.jun.edi.service.response.DeleteServiceResponse;
import jp.co.jun.edi.util.DateUtils;

/**
 * マスタメンテナンス用の仕入先情報を削除するサービス.
 */
@Service
public class MaintSireDeleteService
    extends GenericDeleteService<DeleteServiceParameter<MaintSireReckbnKeyModel>, DeleteServiceResponse> {

    @Autowired
    private MaintSireComponent maintSireComponent;

    @Autowired
    private MKojmstRepository tKojmstRepository;

    @Autowired
    private MSirmstRepository tSirmstRepository;

    @Override
    protected DeleteServiceResponse execute(final DeleteServiceParameter<MaintSireReckbnKeyModel> serviceParameter) {
    	// 区分を取得
    	final String getKbn = serviceParameter.getId().getReckbn();

    	if (getKbn.equals("1")) {

    		// ユーザ情報を取得し、データが存在しない場合は例外を投げる
            final MSirmstEntity sirEntity = maintSireComponent.getMSirmst(serviceParameter.getId());

            // 削除日時を取得
            final Date deletedAt = DateUtils.createNow();

            // 削除日時を設定
            sirEntity.setDeletedAt(deletedAt);

            // メンテ区分の設定（3：削除）
            sirEntity.setMntflg("3");

            // PRD_0148 #10656 add JFE start
            // 送信区分（ロジ）の設定(1:送信対象)
            sirEntity.setSouflga("1");
            // PRD_0148 #10656 add JFE end

          //PRD_0204 add JFE start
            // 銀行コードに固定値設定
            sirEntity.setBank("0000");
            // 支店コードに固定値設定
            sirEntity.setSiten("000");
            // 市町村に固定値設定
            sirEntity.setCity("00000");
            // 費目1に固定値設定
            sirEntity.setHimok1("00");
            // 費目2に固定値設定
            sirEntity.setHimok2("00");
            // 費目3に固定値設定
            sirEntity.setHimok3("00");
            // 費目4に固定値設定
            sirEntity.setHimok4("00");
            // 費目5に固定値設定
            sirEntity.setHimok5("00");
            // 費目6に固定値設定
            sirEntity.setHimok6("00");
            // 費目7に固定値設定
            sirEntity.setHimok7("00");
            // 費目8に固定値設定
            sirEntity.setHimok8("00");
            // 費目9に固定値設定
            sirEntity.setHimok9("00");
            // 費目10に固定値設定
            sirEntity.setHimok10("00");
            // 費目11に固定値設定
            sirEntity.setHimok11("00");
            // 費目12に固定値設定
            sirEntity.setHimok12("00");
            // 費目13に固定値設定
            sirEntity.setHimok13("00");
            // 費目14に固定値設定
            sirEntity.setHimok14("00");
            // 費目15に固定値設定
            sirEntity.setHimok15("00");
            // 費目16に固定値設定
            sirEntity.setHimok16("00");
            // 費目17に固定値設定
            sirEntity.setHimok17("00");
            // 費目18に固定値設定
            sirEntity.setHimok18("00");
            // 費目19に固定値設定
            sirEntity.setHimok19("00");
            // 費目20に固定値設定
            sirEntity.setHimok20("00");
            // 費目21に固定値設定
            sirEntity.setHimok21("00");
            // 費目22に固定値設定
            sirEntity.setHimok22("00");
            // 費目23に固定値設定
            sirEntity.setHimok23("00");
            // 費目24に固定値設定
            sirEntity.setHimok24("00");
            // 費目25に固定値設定
            sirEntity.setHimok25("00");
            // 費目26に固定値設定
            sirEntity.setHimok26("00");
            // 費目27に固定値設定
            sirEntity.setHimok27("00");
            // 費目28に固定値設定
            sirEntity.setHimok28("00");
            // 費目29に固定値設定
            sirEntity.setHimok29("00");
            // 費目30に固定値設定
            sirEntity.setHimok30("00");
            // 振込手数料に固定値設定
            sirEntity.setFtesury("0");
            // 歩引区分に固定値設定
            sirEntity.setBubkbn("0");
            // 送付先区分に固定値設定
            sirEntity.setSofkbn("0");
            // 送付方法に固定値設定
            sirEntity.setSofhou("0");
            // ロック区分に固定値設定
            sirEntity.setLokkbn("0");
            // 送信区分に固定値設定
            sirEntity.setSouflg("0");
            // 入力者に固定値設定
            sirEntity.setTanto("000000");
            // 登録日に固定値設定
            sirEntity.setCrtymd("00000000");
            // 修正日に固定値設定
            sirEntity.setUpdymd("00000000");
            // プログラムＩＤに固定値設定
            sirEntity.setPgid("00000");
            if (org.springframework.util.StringUtils.isEmpty(sirEntity.getYugaiymd())) {
            	sirEntity.setYugaiymd("00000000");
            }
          //PRD_0204 add JFE end

            // 削除日時、メンテ区分を更新
            tSirmstRepository.save(sirEntity);

    	}

        // ユーザ情報を取得し、データが存在しない場合は例外を投げる
        final MKojmstEntity kojEntity = maintSireComponent.getMKojmst(serviceParameter.getId());

        // 削除日時を取得
        final Date deletedAt = DateUtils.createNow();

        // 削除日時を設定
        kojEntity.setDeletedAt(deletedAt);

        // メンテ区分の設定（3：削除）
        kojEntity.setMntflg("3");

        // PRD_0148 #10656 add JFE start
        // 送信区分の設定(1:送信対象)
        kojEntity.setSouflg("1");
        // PRD_0148 #10656 add JFE end

        // 削除日時、メンテ区分を更新
        tKojmstRepository.save(kojEntity);

        return DeleteServiceResponse.builder().build();
    }
}
