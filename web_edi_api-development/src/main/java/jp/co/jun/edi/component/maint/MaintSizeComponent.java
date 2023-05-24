//PRD_0137 #10669 add start
package jp.co.jun.edi.component.maint;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.MSizmstEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.maint.MaintSizeBulkUpdateModel;
import jp.co.jun.edi.repository.DynamicMaintSizeRepository;
import jp.co.jun.edi.repository.MSizmstRepository;
import jp.co.jun.edi.type.MessageCodeType;

/**
 * サイズマスタ用コンポーネント.
 */
@Component
public class MaintSizeComponent {

    @Autowired
    private DynamicMaintSizeRepository dynamicMaintSizeRepository;

	@Autowired
	private MSizmstRepository mSizmstRepository;

    /**
     * サイズ情報登録.
     *
     * @param userId
     *            ユーザID
     * @param record
     *            登録情報
     * @param ｈｓｃｄ
     *            品種コード
     * @return 登録件数
     */
    public int insertMaintCode(final BigInteger userId, final Map<String, String> record,String hscd) {
        return dynamicMaintSizeRepository.insertByMaintCode(userId, record,hscd);
    }

    /**
     * サイズ情報検索.
     *
     * @param userId
     *            ユーザID
     * @param record
     *            登録情報
     * @param ｈｓｃｄ
     *            品種コード
     * @return 登録件数
     */
    public int searchMaintCode(final BigInteger userId, final Map<String, String> record,String hscd) {
        return dynamicMaintSizeRepository.searchByMaintCode(userId, record,hscd);
    }

    /**
     * サイズ情報更新.
     *
     * @param userId
     *            ユーザID
     * @param record
     *            更新情報
     * @param ｈｓｃｄ
     *            品種コード
     * @return 更新件数
     */
    public int updateMaintCode(final BigInteger userId, final Map<String, String> record,String hscd) {
        return dynamicMaintSizeRepository.updateByMaintCode(userId, record,hscd);
    }

    /**
     * メンテコード削除.
     *
     * @param userId
     *            ユーザID
     * @param record
     *            削除情報
     * @param entity
     *            画面構成情報
     * @return 削除件数
     */
    public int deletedMaintCode(final BigInteger userId, final Map<String, String> record) {
        return dynamicMaintSizeRepository.deletedByMaintCode(userId, record);
    }
    /**
     * サイズ情報登録.
     *
     * @param userId
     *            ユーザID
     * @param record
     *            登録情報
     * @param ｈｓｃｄ
     *            品種コード
     * @return 登録件数
     */
    public int searchHscdMaintCode(final BigInteger userId,String hscd) {
        return dynamicMaintSizeRepository.searchHscdMaintCode(userId,hscd);
    }
	//PRD_0154 #10669 add start
	/**
	 * サイズ情報取得.
	 *
	 * @param record
	 *            登録情報
	 * @param ｈｓｃｄ
	 *            品種コード
	 *
	 * @return サイズマスタエンティティ
	 */
	public List<MSizmstEntity> getByHscdAndSzkg(final Map<String, String> record, String hscd) {
		return mSizmstRepository.findByHscdAndSzkg(hscd,record.get("szkg"));
	}

	/**
	 * サイズ情報取得.
	 *
	 * @param bulkUpdateModel
	 *            登録情報
	 *
	 * @return void
	 */
	public void chackDuplicate(MaintSizeBulkUpdateModel bulkUpdateModel) {

		List<Map<String, String>> dataList = bulkUpdateModel.getItems();
		for(int i=0; i<dataList.size(); i++) {
			int duplicate = 0;
			for(int j=0;j<dataList.size(); j++) {
				if(dataList.get(i).get("szkg").equals(dataList.get(j).get("szkg"))){
					 duplicate++;
				}
			}
			if(duplicate>=2) {
				throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_014));
			}
		}
	}
	//PRD_0154 #10669 add end
}
//PRD_0137 #10669 add end