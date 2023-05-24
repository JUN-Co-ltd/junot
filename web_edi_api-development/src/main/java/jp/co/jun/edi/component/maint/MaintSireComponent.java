package jp.co.jun.edi.component.maint;

import java.math.BigInteger;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.component.GenericComponent;
import jp.co.jun.edi.component.model.MaintSireKeyModel;
import jp.co.jun.edi.component.model.MaintSireReckbnKeyModel;
import jp.co.jun.edi.entity.MKojmstEntity;
import jp.co.jun.edi.entity.MSireEntity;
import jp.co.jun.edi.entity.MSirmstEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.maint.MaintSireModel;
import jp.co.jun.edi.repository.MKojmstRepository;
import jp.co.jun.edi.repository.MSireRepository;
import jp.co.jun.edi.repository.MSirmstRepository;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * マスタメンテナンス用の仕入先情報関連のコンポーネント.
 */
@Component
public class MaintSireComponent extends GenericComponent {

    @Autowired
    private MSireRepository tSireRepository;

    @Autowired
    private MKojmstRepository tKojmstRepository;

    @Autowired
    private MSirmstRepository tSirmstRepository;

    /**
     * 取引先登録画面に表示する仕入先情報を取得する.
     *
     * @param key MaintSireKeyModel
     * @return {@link MSireEntity} instance
     * @throws ResourceNotFoundException 仕入先が存在しない場合
     */
    public MSireEntity getMSire(final MaintSireKeyModel key) {
        return tSireRepository.findBySireCodeAndKojCode(key.getSireCode(), key.getKojCode()).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));
    }

    /**
     * 削除対象の工場マスタ情報を取得する.
     *
     * @param key MaintSireKeyModel
     * @return {@link MKojmstEntity} instance
     * @throws ResourceNotFoundException 工場マスタデータが存在しない場合
     */
    public MKojmstEntity getMKojmst(final MaintSireReckbnKeyModel key) {
        return tKojmstRepository.findBySireCodeAndKojCodeIgnoreSystemManaged(key.getSireCode(), key.getKojCode()).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));
    }

    /**
     * 削除対象の仕入先マスタ情報を取得する.
     *
     * @param key MaintSireKeyModel
     * @return {@link MSirmstEntity} instance
     * @throws ResourceNotFoundException 仕入先マスタデータが存在しない場合
     */
    public MSirmstEntity getMSirmst(final MaintSireReckbnKeyModel key) {
        return tSirmstRepository.findBySireCodeIgnoreSystemManaged(key.getSireCode()).orElseThrow(
                () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.CODE_002)));
    }

    /**
     * 工場マスタ情報を保存する.
     *
     * @param entity {@link MKojmstEntity} instance
     * @return {@link MKojmstEntity} instance
     * @throws BusinessException CODE_005 : 仕入先コードと工場コードが重複する仕入先が存在する場合
     */
    public MKojmstEntity mKojmstSave(final MKojmstEntity entity) {
        // 仕入先コードと工場コードが重複する仕入先がいるか検証.
        assertDuplicateKoj(entity.getId(), entity.getSire(), entity.getKojcd());

        return tKojmstRepository.save(entity);
    }

    /**
     * 仕入先コードと工場コードが重複するレコードがあるか検証する.
     *
     * @param userId ユーザID
     * @param sire 仕入先コード
     * @param kojcd 工場コード
     * @throws BusinessException CODE_005 : 仕入先コードと工場コードが重複する仕入先が存在する場合
     */
    public void assertDuplicateKoj(final BigInteger id, final String sire, final String kojcd) {
    	tKojmstRepository.findBySireCodeAndKojCodeIgnoreSystemManaged(sire, kojcd).ifPresent((entity) -> {
            // 仕入先コードと工場コードが重複する仕入先が存在する場合
            if (!Objects.equals(id, entity.getId())) {
                // IDが異なる場合は、例外を投げる
                throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_006));
            }
        });
    }

    /**
     * 仕入先マスタ情報を保存する.
     *
     * @param entity {@link MSirmstEntity} instance
     * @return {@link MSirmstEntity} instance
     * @throws BusinessException CODE_005 : 仕入先コードと工場コードが重複する仕入先が存在する場合
     */
    public MSirmstEntity mSirmstSave(final MSirmstEntity entity) {
        // 仕入先コードが重複する仕入先がいるか検証.
        assertDuplicateSir(entity.getId(), entity.getSire());

        return tSirmstRepository.save(entity);
    }

    /**
     * 仕入先コードが重複するレコードがあるか検証する.
     *
     * @param userId ユーザID
     * @param sire 仕入先コード
     * @throws BusinessException CODE_005 : 仕入先コードと工場コードが重複する仕入先が存在する場合
     */
    public void assertDuplicateSir(final BigInteger id, final String sire) {
    	tSirmstRepository.findBySireCodeIgnoreSystemManaged(sire).ifPresent((entity) -> {
            // 仕入先コードと工場コードが重複する仕入先が存在する場合
            if (!Objects.equals(id, entity.getId())) {
                // IDが異なる場合は、例外を投げる
                throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_006));
            }
        });
    }

    /**
     * {@link MaintSireModel} を {@link MKojmstEntity} にコピーする.
     *
     * @param model {@link MaintSireModel} instance
     * @param entity {@link MKojmstEntity} instance
     */
    public void copyModelToMKojmstEntity(final MaintSireModel model, final MKojmstEntity entity) {
        entity.setReckbn(model.getReckbn());
        entity.setSire(model.getSireCode());
        entity.setKojcd(model.getKojCode());
        entity.setName(model.getKojName());
        entity.setSname(model.getSkojName());
        entity.setSirkbn(model.getSirkbn());
        entity.setYubin(model.getYubin());
        entity.setAdd1(model.getAdd1());
        entity.setAdd2(model.getAdd2());
        entity.setAdd3(model.getAdd3());
        entity.setTel1(model.getTel1());
        entity.setBrand1(model.getBrand1());
        entity.setBrand2(model.getBrand2());
        entity.setBrand3(model.getBrand3());
        entity.setBrand4(model.getBrand4());
        entity.setBrand5(model.getBrand5());
        entity.setBrand6(model.getBrand6());
        entity.setBrand7(model.getBrand7());
        entity.setBrand8(model.getBrand8());
        entity.setBrand9(model.getBrand9());
        entity.setBrand10(model.getBrand10());
        entity.setBrand11(model.getBrand11());
        entity.setBrand12(model.getBrand12());
        entity.setBrand13(model.getBrand13());
        entity.setBrand14(model.getBrand14());
        entity.setBrand15(model.getBrand15());
        entity.setBrand16(model.getBrand16());
        entity.setBrand17(model.getBrand17());
        entity.setBrand18(model.getBrand18());
        entity.setBrand19(model.getBrand19());
        entity.setBrand20(model.getBrand20());
        entity.setBrand21(model.getBrand21());
        entity.setBrand22(model.getBrand22());
        entity.setBrand23(model.getBrand23());
        entity.setBrand24(model.getBrand24());
        entity.setBrand25(model.getBrand25());
        entity.setBrand26(model.getBrand26());
        entity.setBrand27(model.getBrand27());
        entity.setBrand28(model.getBrand28());
        entity.setBrand29(model.getBrand29());
        entity.setBrand30(model.getBrand30());
        entity.setHkiji(model.getHkiji());
        entity.setHseihin(model.getHseihin());
        entity.setHnefuda(model.getHnefuda());
        entity.setHfuzoku(model.getHfuzoku());
        entity.setHsofkbn(model.getHsofkbn());
        entity.setHemail1(model.getHemail1());
        entity.setNsofkbn(model.getNsofkbn());
        entity.setNemail1(model.getNemail1());
        entity.setYsofkbn(model.getYsofkbn());
        entity.setYemail1(model.getYemail1());
    }

    /**
     * {@link MaintSireModel} を {@link MSirmstEntity} にコピーする.
     *
     * @param model {@link MaintSireModel} instance
     * @param entity {@link MSirmstEntity} instance
     */
    public void copyModelToMSirmstEntity(final MaintSireModel model, final MSirmstEntity entity) {
        entity.setSire(model.getSireCode());
        entity.setSirkbn(model.getSirkbn());
        entity.setName(model.getKojName());
        entity.setSname(model.getSkojName());
        entity.setYubin(model.getYubin());
        entity.setAdd1(model.getAdd1());
        entity.setAdd2(model.getAdd2());
        entity.setAdd3(model.getAdd3());
        entity.setTel1(model.getTel1());
        entity.setBrand1(model.getBrand1());
        entity.setBrand2(model.getBrand2());
        entity.setBrand3(model.getBrand3());
        entity.setBrand4(model.getBrand4());
        entity.setBrand5(model.getBrand5());
        entity.setBrand6(model.getBrand6());
        entity.setBrand7(model.getBrand7());
        entity.setBrand8(model.getBrand8());
        entity.setBrand9(model.getBrand9());
        entity.setBrand10(model.getBrand10());
        entity.setBrand11(model.getBrand11());
        entity.setBrand12(model.getBrand12());
        entity.setBrand13(model.getBrand13());
        entity.setBrand14(model.getBrand14());
        entity.setBrand15(model.getBrand15());
        entity.setBrand16(model.getBrand16());
        entity.setBrand17(model.getBrand17());
        entity.setBrand18(model.getBrand18());
        entity.setBrand19(model.getBrand19());
        entity.setBrand20(model.getBrand20());
        entity.setBrand21(model.getBrand21());
        entity.setBrand22(model.getBrand22());
        entity.setBrand23(model.getBrand23());
        entity.setBrand24(model.getBrand24());
        entity.setBrand25(model.getBrand25());
        entity.setBrand26(model.getBrand26());
        entity.setBrand27(model.getBrand27());
        entity.setBrand28(model.getBrand28());
        entity.setBrand29(model.getBrand29());
        entity.setBrand30(model.getBrand30());
        entity.setHkiji(model.getHkiji());
        entity.setHseihin(model.getHseihin());
        entity.setHnefuda(model.getHnefuda());
        entity.setHfuzoku(model.getHfuzoku());
        entity.setDummy2(model.getInOut());
        entity.setYugaikbn(model.getYugaikbn());
        entity.setYugaiymd(model.getYugaiymd());
    }

    /**
     * {@link MSireEntity} を {@link MaintSireModel} にコピーする.
     *
     * @param entity {@link MSireEntity} instance
     * @param model {@link MaintSireModel} instance
     */
    public void copyMSireEntityEntityToModel(final MSireEntity entity, final MaintSireModel model) {
        model.setId(entity.getId());
        model.setReckbn(entity.getReckbn());
        model.setSireCode(entity.getSireCode());
        model.setSireName(entity.getSireName());
        model.setKojCode(entity.getKojCode());
        model.setKojName(entity.getKojName());
        model.setSkojName(entity.getSkojName());
        model.setInOut(entity.getInOut());
        model.setSirkbn(entity.getSirkbn());
        model.setKnktSire(entity.getKnktSire());
        model.setYubin(entity.getYubin());
        model.setAdd1(entity.getAdd1());
        model.setAdd2(entity.getAdd2());
        model.setAdd3(entity.getAdd3());
        model.setTel1(entity.getTel1());
        model.setYugaikbn(entity.getYugaikbn());
        model.setYugaiymd(entity.getYugaiymd());
        model.setBrand1(entity.getBrand1());
        model.setBrand2(entity.getBrand2());
        model.setBrand3(entity.getBrand3());
        model.setBrand4(entity.getBrand4());
        model.setBrand5(entity.getBrand5());
        model.setBrand6(entity.getBrand6());
        model.setBrand7(entity.getBrand7());
        model.setBrand8(entity.getBrand8());
        model.setBrand9(entity.getBrand9());
        model.setBrand10(entity.getBrand10());
        model.setBrand11(entity.getBrand11());
        model.setBrand12(entity.getBrand12());
        model.setBrand13(entity.getBrand13());
        model.setBrand14(entity.getBrand14());
        model.setBrand15(entity.getBrand15());
        model.setBrand16(entity.getBrand16());
        model.setBrand17(entity.getBrand17());
        model.setBrand18(entity.getBrand18());
        model.setBrand19(entity.getBrand19());
        model.setBrand20(entity.getBrand20());
        model.setBrand21(entity.getBrand21());
        model.setBrand22(entity.getBrand22());
        model.setBrand23(entity.getBrand23());
        model.setBrand24(entity.getBrand24());
        model.setBrand25(entity.getBrand25());
        model.setBrand26(entity.getBrand26());
        model.setBrand27(entity.getBrand27());
        model.setBrand28(entity.getBrand28());
        model.setBrand29(entity.getBrand29());
        model.setBrand30(entity.getBrand30());
        model.setHkiji(entity.getHkiji());
        model.setHseihin(entity.getHseihin());
        model.setHnefuda(entity.getHnefuda());
        model.setHfuzoku(entity.getHfuzoku());
        model.setHsofkbn(entity.getHsofkbn());
        model.setHemail1(entity.getHemail1());
        model.setNsofkbn(entity.getNsofkbn());
        model.setNemail1(entity.getNemail1());
        model.setYsofkbn(entity.getYsofkbn());
        model.setYemail1(entity.getYemail1());
    }

}
