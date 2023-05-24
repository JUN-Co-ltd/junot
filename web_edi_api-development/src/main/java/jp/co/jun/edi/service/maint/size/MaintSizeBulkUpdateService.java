//PRD_0137 #10669 add start
package jp.co.jun.edi.service.maint.size;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jp.co.jun.edi.component.maint.MaintSizeComponent;
import jp.co.jun.edi.entity.MSizmstEntity;
import jp.co.jun.edi.exception.BusinessException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.model.maint.MaintSizeBulkResponseModel;
import jp.co.jun.edi.model.maint.MaintSizeBulkUpdateModel;
import jp.co.jun.edi.model.maint.code.MaintCountCUDCountModel;
import jp.co.jun.edi.security.CustomLoginUser;
import jp.co.jun.edi.service.GenericCreateService;
import jp.co.jun.edi.service.parameter.MaintSizeBulkUpdateServiceParameter;
import jp.co.jun.edi.service.response.GetServiceResponse;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.LogStringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * サイズ情報を更新するサービス.
 */
@Slf4j
@Service
public class MaintSizeBulkUpdateService extends
		GenericCreateService<MaintSizeBulkUpdateServiceParameter, GetServiceResponse<MaintSizeBulkResponseModel>> {

	@Autowired
	private MaintSizeComponent maintSizeComponent;

	@Override
	protected GetServiceResponse<MaintSizeBulkResponseModel> execute(
			final MaintSizeBulkUpdateServiceParameter serviceParameter) {
		log.info(LogStringUtil.of("execute")
				.message("Start processing of UpsertSizeMaster.")
				.build());
		final CustomLoginUser user = serviceParameter.getLoginUser();
		final Boolean copyflg = serviceParameter.getCopyFlg();
		final MaintSizeBulkUpdateModel bulkUpdateModel = serviceParameter.getBulkUpdateModel();
		final String hscd = bulkUpdateModel.getHscd();
		int updateCnt = 0;
		int registCnt = 0;

		//PRD_0154 #10699 add start
		maintSizeComponent.chackDuplicate(bulkUpdateModel);
		//PRD_0154 #10699 add end

		//PRD_0154 #10699 mod start

		if (copyflg) {

			//コピー新規の場合、コピー先の品種へ既にサイズ登録が1件でもされている場合エラーにする。
			int recordCount = maintSizeComponent.searchHscdMaintCode(user.getUserId(), hscd);
			if (recordCount > 0) {
				throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_015));
			}
		}
			// 1レコードずつ登録／更新する
			for (Map<String, String> data : bulkUpdateModel.getItems()) {

			//				int exist = maintSizeComponent.searchMaintCode(user.getUserId(), data, hscd);
			//				if (exist > 0) {
			//					throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_014));
//				if (data.get("id") == null || data.get("id") == "" || (data.get("id") != "" && copyflg)) {
			//					// 登録
			//					maintSizeComponent.insertMaintCode(user.getUserId(), data, hscd);
			//					registCnt++;
			//				} else {
			//					// 更新
			//					maintSizeComponent.updateMaintCode(user.getUserId(), data, hscd);
			//					updateCnt++;
			//				}
			//			}
			//
			// 品種コード＋サイズで存在チェック
	        final List<MSizmstEntity> entities =
	        		maintSizeComponent.getByHscdAndSzkg(data,hscd);

			if (entities.isEmpty()) {
				//取得したデータの個数が1未満＝サイズ：品種コードの組み合わせが見つからなかった場合
				if (!data.get("id").isEmpty() && !copyflg) {
					//更新行の場合：更新データ側のIDで論理削除
					//行わない場合、更新元のデータが残ってしまう
					maintSizeComponent.deletedMaintCode(user.getUserId(), data);
				}
				//登録処理
					maintSizeComponent.insertMaintCode(user.getUserId(), data, hscd);
					registCnt++;
				} else {
				//取得したデータのIDと更新データ側のIDが同一だった場合
				if (entities.get(0).getId().equals(data.get("id"))) {
					//表示順の変更であるため、更新処理を行う
					data.put("mntflg", "2");
					maintSizeComponent.updateMaintCode(user.getUserId(), data, hscd);
					updateCnt++;
				}
				//検索結果が削除済みの場合、取得したデータ側のIDをキーとして削除済みのデータを有効に
				else if (!data.get("id").isEmpty() && entities.get(0).getDeletedAt() != null) {
					//更新データ側のIDで論理削除処理を行う
					//行わない場合、更新元のデータが残ってしまう
					maintSizeComponent.deletedMaintCode(user.getUserId(), data);
					data.put("id", entities.get(0).getId());
					data.put("mntflg", "1");
					maintSizeComponent.updateMaintCode(user.getUserId(), data, hscd);
					updateCnt++;
			}
				//検索結果が削除済みの場合、取得したデータのIDをキーとして削除済みのデータを有効に
				//更新データ側のIDが空の場合(新規行の場合)、論理削除しなくても問題ないので更新処理のみ行う
				else if (data.get("id").isEmpty() && entities.get(0).getDeletedAt() != null) {
					data.put("id", entities.get(0).getId());
					data.put("mntflg", "1");
					maintSizeComponent.updateMaintCode(user.getUserId(), data, hscd);
					updateCnt++;
				}
				//既存の有効なサイズと重複している場合
				else {
					//重複エラー投げる
					throw new BusinessException(ResultMessages.warning().add(MessageCodeType.CODE_014));
				}
			}
		} //PRD_0154 #10699 mod end

		// レスポンス情報
		final MaintSizeBulkResponseModel model = new MaintSizeBulkResponseModel();
		model.setSuccess(new MaintCountCUDCountModel());
		model.getSuccess().setUpdated(updateCnt);
		model.getSuccess().setRegisted(registCnt);

		log.info(LogStringUtil.of("execute")
				.message("End processing of UpsertSizeMaster.")
				.build());
		return GetServiceResponse.<MaintSizeBulkResponseModel> builder().item(model).build();
	}

}
//PRD_0137 #10669 add end