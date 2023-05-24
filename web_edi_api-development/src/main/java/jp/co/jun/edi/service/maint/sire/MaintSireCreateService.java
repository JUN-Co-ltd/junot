package jp.co.jun.edi.service.maint.sire;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.maint.MaintSireComponent;
import jp.co.jun.edi.entity.MKojmstEntity;
import jp.co.jun.edi.entity.MSirmstEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.maint.MaintSireModel;
import jp.co.jun.edi.service.GenericCreateService;
import jp.co.jun.edi.service.parameter.CreateServiceParameter;
import jp.co.jun.edi.service.response.CreateServiceResponse;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * マスタメンテナンス用の工場マスタ情報を作成するサービス.
 */
@Service
public class MaintSireCreateService
    extends GenericCreateService<CreateServiceParameter<MaintSireModel>, CreateServiceResponse<MaintSireModel>> {

    @Autowired
    private MaintSireComponent maintSireComponent;

    @Override
    protected CreateServiceResponse<MaintSireModel> execute(final CreateServiceParameter<MaintSireModel> serviceParameter) {

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

        final MKojmstEntity kojEntity = new MKojmstEntity();

    	if (getKbn.equals("1")) {

    		final MSirmstEntity sirEntity = new MSirmstEntity();

    		// モデルからエンティティへコピー
            maintSireComponent.copyModelToMSirmstEntity(model, sirEntity);

            // メンテ区分の設定（1：登録）
            sirEntity.setMntflg("1");
            // ＦＡＸ１に空白設定
            sirEntity.setFax1("");
          //PRD_0204 mod JFE start
            // 銀行コードに空白設定
            //sirEntity.setBank("");
            sirEntity.setBank("0000");
            // 支店コードに空白設定
            //sirEntity.setSiten("");
            sirEntity.setSiten("000");
          //PRD_0204 mod JFE end
            // 口座種別に空白設定
            sirEntity.setKozsyu("");
            // 口座番号に空白設定
            sirEntity.setKozno("");
            // 口座名義人（漢字）に空白設定
            sirEntity.setMeigij("");
            // 口座名義人（カナ）に空白設定
            sirEntity.setMeigik("");
          //PRD_0204 mod JFE start
            // 市町村に空白設定
            //sirEntity.setCity("");
            sirEntity.setCity("00000");
          //PRD_0204 mod JFE end
            // 予備１に空白設定
            sirEntity.setDummy1("");
          //PRD_0204 mod JFE start
            // 費目1に空白設定
            //sirEntity.setHimok1("");
            sirEntity.setHimok1("00");
            // 費目2に空白設定
            //sirEntity.setHimok2("");
            sirEntity.setHimok2("00");
            // 費目3に空白設定
            //sirEntity.setHimok3("");
            sirEntity.setHimok3("00");
            // 費目4に空白設定
            //sirEntity.setHimok4("");
            sirEntity.setHimok4("00");
            // 費目5に空白設定
            //sirEntity.setHimok5("");
            sirEntity.setHimok5("00");
            // 費目6に空白設定
            //sirEntity.setHimok6("");
            sirEntity.setHimok6("00");
            // 費目7に空白設定
            //sirEntity.setHimok7("");
            sirEntity.setHimok7("00");
            // 費目8に空白設定
            //sirEntity.setHimok8("");
            sirEntity.setHimok8("00");
            // 費目9に空白設定
            //sirEntity.setHimok9("");
            sirEntity.setHimok9("00");
            // 費目10に空白設定
            //sirEntity.setHimok10("");
            sirEntity.setHimok10("00");
            // 費目11に空白設定
            //sirEntity.setHimok11("");
            sirEntity.setHimok11("00");
            // 費目12に空白設定
            //sirEntity.setHimok12("");
            sirEntity.setHimok12("00");
            // 費目13に空白設定
            //sirEntity.setHimok13("");
            sirEntity.setHimok13("00");
            // 費目14に空白設定
            //sirEntity.setHimok14("");
            sirEntity.setHimok14("00");
            // 費目15に空白設定
            //sirEntity.setHimok15("");
            sirEntity.setHimok15("00");
            // 費目16に空白設定
            //sirEntity.setHimok16("");
            sirEntity.setHimok16("00");
            // 費目17に空白設定
            //sirEntity.setHimok17("");
            sirEntity.setHimok17("00");
            // 費目18に空白設定
            //sirEntity.setHimok18("");
            sirEntity.setHimok18("00");
            // 費目19に空白設定
            //sirEntity.setHimok19("");
            sirEntity.setHimok19("00");
            // 費目20に空白設定
            //sirEntity.setHimok20("");
            sirEntity.setHimok20("00");
            // 費目21に空白設定
            //sirEntity.setHimok21("");
            sirEntity.setHimok21("00");
            // 費目22に空白設定
            //sirEntity.setHimok22("");
            sirEntity.setHimok22("00");
            // 費目23に空白設定
            //sirEntity.setHimok23("");
            sirEntity.setHimok23("00");
            // 費目24に空白設定
            //sirEntity.setHimok24("");
            sirEntity.setHimok24("00");
            // 費目25に空白設定
            //sirEntity.setHimok25("");
            sirEntity.setHimok25("00");
            // 費目26に空白設定
            //sirEntity.setHimok26("");
            sirEntity.setHimok26("00");
            // 費目27に空白設定
            //sirEntity.setHimok27("");
            sirEntity.setHimok27("00");
            // 費目28に空白設定
            //sirEntity.setHimok28("");
            sirEntity.setHimok28("00");
            // 費目29に空白設定
            //sirEntity.setHimok29("");
            sirEntity.setHimok29("00");
            // 費目30に空白設定
            //sirEntity.setHimok30("");
            sirEntity.setHimok30("00");
            // 振込手数料に空白設定
            //sirEntity.setFtesury("");
            sirEntity.setFtesury("0");
            // 歩引区分に空白設定
            //sirEntity.setBubkbn("");
            sirEntity.setBubkbn("0");
            // 送付先区分に空白設定
            //sirEntity.setSofkbn("");
            sirEntity.setSofkbn("0");
            // 送付方法に空白設定
            //sirEntity.setSofhou("");
            sirEntity.setSofhou("0");
            // ロック区分に空白設定
            //sirEntity.setLokkbn("");
            sirEntity.setLokkbn("0");
            // 送信区分に空白設定
            //sirEntity.setSouflg("");
            sirEntity.setSouflg("0");
            // 入力者に空白設定
            //sirEntity.setTanto("");
            sirEntity.setTanto("000000");
            // 登録日に空白設定
            //sirEntity.setCrtymd("");
            sirEntity.setCrtymd("00000000");
            // 修正日に空白設定
            //sirEntity.setUpdymd("");
            sirEntity.setUpdymd("00000000");
            // プログラムＩＤに空白設定
            //sirEntity.setPgid("");
            sirEntity.setPgid("00000");
          //PRD_0204 mod JFE end
            // PRD_0148 #10656 mod JFE start
            // 送信区分（ロジ）に空白設定
            //sirEntity.setSouflga("");
            // 送信区分（ロジ）の設定(1:送信対象)
            sirEntity.setSouflga("1");
            // PRD_0148 #10656 mod JFE end
            // 送信日（ロジ）に空白設定
            sirEntity.setSouymda("");
          //PRD_0204 add JFE start
            if (org.springframework.util.StringUtils.isEmpty(sirEntity.getYugaiymd())) {
            	sirEntity.setYugaiymd("00000000");
            }
          //PRD_0204 add JFE end

            // 仕入先マスタ情報を保存
            maintSireComponent.mSirmstSave(sirEntity);
    	}


        // モデルからエンティティへコピー
        maintSireComponent.copyModelToMKojmstEntity(model, kojEntity);

        // メンテ区分の設定（1：登録）
        kojEntity.setMntflg("1");
        // ＦＡＸ番号１に空白設定
        kojEntity.setFax1("");
        // 発注書送付先ＦＡＸに空白設定
        kojEntity.setHfax("");
        // 納品依頼書送付先ＦＡＸに空白設定
        kojEntity.setNFax("");
        // 予備　受領書送付先ＦＡＸに空白設定
        kojEntity.setYfax("");
        // 仕入伝票に空白設定
        kojEntity.setSdenflg("");
        // PRD_0148 #10656 mod JFE start
        // 送信区分に空白設定
        //kojEntity.setSouflg("");
        // 送信区分の設定(1:送信対象)
        kojEntity.setSouflg("1");
        // PRD_0148 #10656 mod JFE end
        // 入力者に空白設定
        kojEntity.setTanto("");
        // 登録日に空白設定
        kojEntity.setCrtymd("");
        // 修正日に空白設定
        kojEntity.setUpdymd("");
        // プログラムＩＤに空白設定
        kojEntity.setPgid("");

        // 工場マスタ情報を保存
        maintSireComponent.mKojmstSave(kojEntity);

        // 登録されたIDを設定
        //kojModel.setId(kojEntity.getId());

        return CreateServiceResponse.<MaintSireModel>builder().item(model).build();
    }
}
