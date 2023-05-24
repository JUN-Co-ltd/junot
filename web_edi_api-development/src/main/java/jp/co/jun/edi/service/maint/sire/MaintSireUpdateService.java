package jp.co.jun.edi.service.maint.sire;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.maint.MaintSireComponent;
import jp.co.jun.edi.entity.MKojmstEntity;
import jp.co.jun.edi.entity.MSirmstEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.maint.MaintSireModel;
import jp.co.jun.edi.repository.MKojmstRepository;
import jp.co.jun.edi.repository.MSirmstRepository;
import jp.co.jun.edi.service.GenericUpdateService;
import jp.co.jun.edi.service.parameter.UpdateServiceParameter;
import jp.co.jun.edi.service.response.UpdateServiceResponse;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * マスタメンテナンス用の仕入先情報を更新するサービス.
 */
@Service
public class MaintSireUpdateService
    extends GenericUpdateService<UpdateServiceParameter<MaintSireModel>, UpdateServiceResponse<MaintSireModel>> {

    @Autowired
    private MaintSireComponent maintSireComponent;

    @Autowired
    private MKojmstRepository tKojmstRepository;

    @Autowired
    private MSirmstRepository tSirmstRepository;

    @Override
    protected UpdateServiceResponse<MaintSireModel> execute(final UpdateServiceParameter<MaintSireModel> serviceParameter) {
        final MaintSireModel model = serviceParameter.getItem();

        final String getKbn = model.getReckbn();
        // 国内・国外
    	final String getInOut = model.getInOut();
    	// 工場名・正式名のバイト数
    	final int getKojNameBytes = model.getKojName().getBytes(StandardCharsets.UTF_8).length;
    	// 工場名・省略名のバイト数
    	final int getSkojnameBytes = model.getSkojName().getBytes(StandardCharsets.UTF_8).length;
    	// 国外の場合
    	if (getInOut.equals("1")) {
    		// 正式名が全角30文字分以内で入力されていない場合はエラー
    		if (getKojNameBytes > 90) {
    			throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_MS_001));
    		}
    		// 略名が全角10文字分以内で入力されていない場合はエラー
    		if (getSkojnameBytes > 30) {
    			throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_MS_002));
    		}
    	}

        // 仕入先の場合
        if (getKbn.equals("1")) {
        	// 仕入先マスタ情報を取得し、データが存在しない場合は例外を投げる
            final MSirmstEntity sirEntity = tSirmstRepository
        	.findBySireCodeIgnoreSystemManaged(model.getSireCode())
        	.orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

            // モデルからエンティティへコピー
            maintSireComponent.copyModelToMSirmstEntity(model, sirEntity);

            // メンテ区分の設定（2：修正）
            sirEntity.setMntflg("2");

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

            // 仕入先マスタ情報を保存
            maintSireComponent.mSirmstSave(sirEntity);
        }

        // 工場マスタ情報を取得し、データが存在しない場合は例外を投げる
        final MKojmstEntity kojEntity = tKojmstRepository
    	.findBySireCodeAndKojCodeIgnoreSystemManaged(model.getSireCode(), model.getKojCode())
    	.orElseThrow(() -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));

        // モデルからエンティティへコピー
        maintSireComponent.copyModelToMKojmstEntity(model, kojEntity);

        // メンテ区分の設定（2：修正）
        kojEntity.setMntflg("2");

        // PRD_0148 #10656 add JFE start
        // 送信区分の設定(1:送信対象)
        kojEntity.setSouflg("1");
        // PRD_0148 #10656 add JFE end

        // 工場マスタ情報を保存
        maintSireComponent.mKojmstSave(kojEntity);

        return UpdateServiceResponse.<MaintSireModel>builder().item(model).build();
    }
}
