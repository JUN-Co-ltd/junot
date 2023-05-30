import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
// PRD_0031 mod SIT start
//import { FormGroup, AbstractControl, ValidationErrors } from '@angular/forms';
import { FormArray, FormGroup, AbstractControl, ValidationErrors } from '@angular/forms';
// PRD_0031 mod SIT end
import { HttpErrorResponse } from '@angular/common/http';

import { NgbDateStruct, NgbDateParserFormatter } from '@ng-bootstrap/ng-bootstrap';

import { catchError } from 'rxjs/operators';
import { Observable } from 'rxjs';

// PRD_0033 mod SIT start
//import { DeliveryApprovalStatus, Path, CarryType, DeliveryVoucherCategoryType } from '../../const/const';
import { DeliveryApprovalStatus, Path, CarryType, DeliveryVoucherCategoryType, StorageKey } from '../../const/const';
// PRD_0033 mod SIT end

import { BusinessUtils } from '../../util/business-utils';
import { DateUtils } from '../../util/date-utils';
import { CalculationUtils } from '../../util/calculation-utils';
// PRD_0033 add SIT start
import { BooleanUtils } from '../../util/boolean-utils';
// PRD_0033 add SIT end

import { MKanmstService } from '../../service/m-kanmst.service';
import { DeliveryService } from '../../service/bo/delivery.service';
import { FileService } from 'src/app/service/file.service';
import { MessageConfirmModalService } from 'src/app/service/message-confirm-modal.service';
// PRD_0033 add SIT start
import { LocalStorageService } from '../../service/local-storage.service';
// PRD_0033 add SIT end

import { JunpcHrtmst } from '../../model/junpc-hrtmst';
import { MKanmst } from '../../model/m-kanmst';
import { Item } from '../../model/item';
import { Order } from '../../model/order';
import { DeliveryPlan } from '../../model/delivery-plan';
import { Delivery } from '../../model/delivery';
import { DeliveryDetail } from 'src/app/model/delivery-detail';
import { DeliveryVoucherFileInfo } from 'src/app/model/delivery-voucher-file-info';
// PRD_0033 add SIT start
import { Session } from '../../model/session';
// PRD_0033 add SIT end
// PRD_0123 #7054 add JFE start
import { MdeliveryLocation } from 'src/app/model/m-delivery-location';
// PRD_0123 #7054 add JFE end
@Component({
  selector: 'app-delivery-header',
  templateUrl: './delivery-header.component.html',
  styleUrls: ['./delivery-header.component.scss']
})
export class DeliveryHeaderComponent implements OnInit {

  /** 品番情報. */
  @Input() itemData: Item;

  /** 発注情報. */
  @Input() orderData: Order;

  /** 納品履歴リスト. */
  @Input() deliveryHistoryList: Delivery[] = [];

  /** 納品予定. */
  @Input() deliveryPlan: DeliveryPlan = null;

  /** 配分率リスト. */
  @Input() distributionRatioMastaList: JunpcHrtmst[];

  /** 納品承認ステータス. */
  @Input() private deliveryApproveStatus: string;

  /** 直送フラグ. */
  @Input() isDirectDelivery = false;

  /** パス. */
  @Input() path = '';

  /** submitボタン押下したか. */
  @Input() submitted = false;

  /** メインのフォーム. */
  @Input() mainForm: FormGroup;

  /** 得意先配分画面フラグ. */
  @Input() isDeliveryStore = false;

  /** 仕入数超過. */
  @Input() overPerchase = false;

  /** 納品明細リスト. */
  @Input() private deliveryDetails: DeliveryDetail[] = null;

  /** 納品伝票ファイルリスト. */
  @Input() private deliveryVoucherFileInfos: DeliveryVoucherFileInfo[] = [];

  // PRD_0033 add SIT start
  /** ログインユーザーのセッション情報 */
  @Input() private session: Session;
  // PRD_0033 add SIT end

  // PRD_0123 #7054 add JFE start
  /** 納入場所リスト */
  @Input() deliveryLocationList: MdeliveryLocation[] = [];
  /** 納入場所リスト表示フラグ */
  @Input() isShowDeliveryLocationList = true;
  // PRD_0123 #7054 add JFE end
  @Output() private distributeTypeIndex: EventEmitter<number> = new EventEmitter();
  @Output() private distribute: EventEmitter<number> = new EventEmitter();
  @Output() private clearDeliveryLotValue: EventEmitter<void> = new EventEmitter();
  @Output() private inputDeliveryAt: EventEmitter<void> = new EventEmitter();
  @Output() private handleFatalApiError: EventEmitter<HttpErrorResponse> = new EventEmitter();
  // PRD_0031 add SIT start
  @Output() private distributeDownload: EventEmitter<void> = new EventEmitter();
  @Output() private distributeUpload: EventEmitter<any> = new EventEmitter();
  // PRD_0031 add SIT end

  /** 左側のラベル. */
  leftLabel = '';

  /** 右側のラベル. */
  rightLabel = '';

  /** 納品済数：納品依頼済数. */
  allDeliveredLot = 0;

  /** 納品残数：裁断数 - 日付が決まっている納品予定skuの合計数.  */
  remainingLot = 0;

  /** 増減産数：納品予定skuの合計数  - 発注数. */
  changesInLot = 0;

  /** 増減産数(％)：増減産数 / 発注数.  */
  changesInLotRatio = '';

  /** 編集中の品番のサブシーズン名. */
  subSeasonName = '';

  /** 納期の入力可能範囲最小日. */
  minDeliveryAt: NgbDateStruct;

  /** 納期の入力可能範囲最大日. */
  maxDeliveryAt: NgbDateStruct;

  /** 出荷配分伝票. */
  shippingDistributionVoucher: DeliveryVoucherFileInfo = null;

  /** ピッキングリスト表示フラグ. */
  pickingList: DeliveryVoucherFileInfo = null;

  // PRD_0033 add SIT start
  /** 検索項目表示フラグ */
  isSearchItemsCollapsed = false;

  /** アップロードボタン押下可否フラグ */
  isDistributeUpload = true;
  // PRD_0033 add SIT end

  constructor(
    private ngbDateParserFormatter: NgbDateParserFormatter,
    private mKanmstService: MKanmstService,
    private deliveryService: DeliveryService,
    private messageConfirmModalService: MessageConfirmModalService,
    private fileService: FileService,
    // PRD_0033 add SIT start
    private localStorageService: LocalStorageService
    // PRD_0033 add SIT end
  ) { }

  /**
   * mainFormの項目の状態を取得する。
   * @return mainForm.controls
   */
  get fCtrl(): { [key: string]: AbstractControl } {
    return this.mainForm.controls;
  }

  /**
   * mainFormのerrorsを取得する。
   * @return mainForm.errors
   */
  get fErr(): ValidationErrors {
    return this.mainForm.errors;
  }

  ngOnInit() {
    this.initDisplayData();
  }

  /**
   * 初期表示処理.
   * @returns Promise<void>
   */
  private async initDisplayData(): Promise<void> {
    this.setLabel(this.deliveryApproveStatus);
    this.getDeliveryAtDateRange();
    this.subSeasonName = BusinessUtils.getSeasonValue(this.itemData.subSeasonCode);  // サブシーズンの設定
    this.calculateAggregate(this.deliveryPlan); // 集計処理
    this.showFileDownLoadIcon(this.deliveryVoucherFileInfos); // 納品伝票ファイルダウンロードアイコン表示制御
    // PRD_0031 add SIT start
    this.isDistributeUpload = !(
        this.mainForm.disabled === true ||
        (this.mainForm.get('deliveryStores') as FormArray).controls.every(store => store.disabled === true) ||
      (this.mainForm.get('deliveryStores') as FormArray).controls.every(store =>
        (store.get('deliveryStoreSkus') as FormArray).controls.every(storeSku => storeSku.disabled === true))
    );
    // PRD_0031 add SIT end
  }

  /**
   * ラベルの文字列設定.
   * @param deliveryApproveStatus 承認状態
   */
  private setLabel(deliveryApproveStatus: string): void {
    this.leftLabel = this.deliveryDetails.some(dd => true === dd.arrivalFlg) ?
      '仕入済' : this.generateApproveLabel(deliveryApproveStatus);

    if (!this.isDeliveryStore) { return; }

    if (this.deliveryDetails.some(dd => true === dd.storeRegisteredFlg)) {
      this.rightLabel = this.overPerchase ? '要再配分' : '配分済';
      return;
    }

    this.rightLabel = '未配分';
  }

  /**
   * @param deliveryApproveStatus 承認状態
   * @returns 承認状態のラベル文言.
   */
  private generateApproveLabel(deliveryApproveStatus: string): string {
    switch (deliveryApproveStatus) {
      case DeliveryApprovalStatus.ACCEPT:
        return '承認済';
      case DeliveryApprovalStatus.REJECT:
        return '差戻';
      default:
        return '未承認';
    }
  }

  /**
   * 納期のカレンダー日付入力範囲設定.
   * @returns Observable<void>
   */
  private getDeliveryAtDateRange(): void {
    // 管理マスタを取得
    this.mKanmstService.getKanmst().subscribe(
      kanriMstData => this.setDeliveryAtLimit(kanriMstData),
      (error: HttpErrorResponse) => this.onFatalApiError(error));
  }

  /**
   * 取得した管理マスタから納期入力可能範囲最小日、最大日を設定する.
   * @param kanriMstData 管理マスタ
   */
  private setDeliveryAtLimit(kanriMstData: MKanmst): void {
    console.debug('getKanmst:', kanriMstData);
    this.minDeliveryAt = this.ngbDateParserFormatter.parse(CalculationUtils.calcStartDeliveryAt(kanriMstData.nitymd));
    this.maxDeliveryAt = this.ngbDateParserFormatter.parse(CalculationUtils.calcEndDeliveryAt(kanriMstData.simymd));
  }

  /**
   * 集計部分の計算を行う.
   * @param deliveryPlan 納品予定情報
   */
  private calculateAggregate(deliveryPlan?: DeliveryPlan): void {
    const result = this.deliveryService.calculateAggregate(deliveryPlan, this.orderData, this.deliveryHistoryList);
    this.allDeliveredLot = result.allDeliveredLot;      // 納品済数
    this.remainingLot = result.remainingLot;            // 納品残数
    this.changesInLot = result.changesInLot;            // 増減産数
    this.changesInLotRatio = result.changesInLotRatio;  // 増減産率※小数2桁まで(四捨五入)。発注数量0はありえない
  }

  /**
   * 配分区分変更時の処理.
   * @param selectedRatioIndex 選択された配分率区分のindex
   */
  onChangeDistributionType(selectedRatioIndex: number): void {
    this.distributeTypeIndex.emit(selectedRatioIndex);
  }

  /**
   * 配分ボタン押下時の処理.
   * @param selectedRatioIndex 選択された配分率区分のindex
   */
  onClickDistributionBtn(selectedRatioIndex: number): void {
    this.distribute.emit(selectedRatioIndex);
  }

  /**
   * 納期入力時の処理.
   */
  onInputDeliveryAt(): void {
    this.inputDeliveryAt.emit();
  }

  /**
   * 致命的APIエラー時の処理.
   * @param error エラーレスポンス
   */
  private onFatalApiError(error: HttpErrorResponse): void {
    this.handleFatalApiError.emit(error);
  }

  /**
   * 日付フォーカスアウト時処理.
   * @param maskedValue 変換済の値
   * @param type 日付項目名
   */
  onBlurDate(maskedValue: string, type: string): void {
    // patchValueしないとpipeの変換値がformにセットされない
    const ngbDate = DateUtils.onBlurDate(maskedValue);

    if (ngbDate) {
      this.mainForm.patchValue({ [type]: ngbDate });
    }
  }

  /**
   * クリアボタン押下時の処理.
   */
  onClearDeliveryLotValue(): void {
    this.mainForm.markAsDirty();
    this.clearDeliveryLotValue.emit();
  }

  /**
   * 納品予定明細閲覧画面を別ウィンドウで表示.
   */
  onDeliveryPlanDetaiView(): void {
    const deliveryPlanDetailURL = `/deliveryPlans/show/${ this.orderData.id }`;
    window.open(deliveryPlanDetailURL, null, 'width=500,toolbar=yes,menubar=yes,scrollbars=yes');
  }

  /**
   * 納期・数量未入力エラーメッセージを表示するか.
   * 更新・訂正処理時は納期と納品数量が未入力の場合に削除処理とする為エラーにしない.
   * ※課別配分画面では訂正時はエラーにする.
   * 課別：
   *  登録：表示する
   *  編集：表示しない
   *  訂正：表示する
   * 店舗別
   *  登録：表示する
   *  編集：表示しない
   *  訂正：表示しない
   * @returns true:表示する
   */
  showDeliveryAtLotRequiredErrorMessage(): boolean {
    return this.fErr != null
      && this.fErr.deliveryAtLotRequired != null
      && (
        this.path === Path.NEW
        || (!this.isDeliveryStore && this.path === Path.CORRECT)
      );
  }

  /**
   * 納品伝票ファイル設定.
   * ※※納品依頼承認済かつ直送の場合のみ表示※※
   * ・直送ラベル
   * ・出荷配分伝票がある場合は出荷配分伝票DLアイコン表示。ファイル情報セット。
   * ・ピッキングリストがある場合はピッキングリストDLアイコン表示。ファイル情報セット。
   * @param deliveryVoucherFileInfos 納品伝票ファイル情報リスト
   */
  private showFileDownLoadIcon(deliveryVoucherFileInfos: DeliveryVoucherFileInfo[]): void {
    if (this.deliveryApproveStatus !== DeliveryApprovalStatus.ACCEPT || !this.isDirectDelivery) {
      // 納品依頼未承認 または 直送でない場合はアイコン非表示
      return;
    }

    // 出荷配分伝票のセット
    this.shippingDistributionVoucher = deliveryVoucherFileInfos.find(deliveryVoucherFile =>
      deliveryVoucherFile.voucherCategory === DeliveryVoucherCategoryType.SHIPPING_DISTRIBUTION_VOUCHER);

    // ピッキングリストのセット
    this.pickingList = deliveryVoucherFileInfos.find(deliveryVoucherFile =>
      deliveryVoucherFile.voucherCategory === DeliveryVoucherCategoryType.PICKING_LIST);
  }

  /**
   * 納品伝票ファイルダウンロードリンク押下処理.
   * @param fileId ファイルID
   */
  onDeliveryVoucherFileDownLoad(fileId: number): void {
    this.fileService.downloadFile(fileId).pipe(catchError(this.showErrorModal)).subscribe();
  }

  /**
   * エラーモーダルを表示する.
   * @param error エラー情報
   * @returns エラーモーダルの結果
   * - true : 「OK」が押された場合
   * - false : モーダルが閉じられた場合
   */
  private showErrorModal = (error: any): Observable<boolean> => this.messageConfirmModalService.openErrorModal(error);

  // PRD_0031 add SIT start
  /**
  * ダウンロードボタン押下時の処理
  */
   onDistributeDownload(): void {
    this.distributeDownload.emit();
   }

  /**
  * アップロードボタン押下時の処理
  */
  onDistributeUpload(evt): void {
    const file = evt.target.files[0];
    this.distributeUpload.emit(file);
    const obj = <HTMLInputElement>document.getElementById("distributeUpload");
    obj.value = "";
   }
  // PRD_0031 add SIT end
}
