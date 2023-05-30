import { Component, OnInit, Input } from '@angular/core';
import { NgbActiveModal, NgbDatepickerI18n, NgbDateParserFormatter } from '@ng-bootstrap/ng-bootstrap';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

import { NgbDateFRParserFormatter } from '../../lib/ngb-date-fr-parser-formatter';

import { ProductionStatusType } from '../../const/const';
import { StringUtils } from '../../util/string-utils';
import { DateUtils } from '../../util/date-utils';
import { ExceptionUtils } from '../../util/exception-utils';
import { AuthUtils } from 'src/app/util/auth-utils';

import { ProductionStatusHistory } from '../../model/production-status-history';
import { ProductionStatusHistorySearchCondition } from '../../model/production-status-history-search-condition';
import { ProductionStatus } from '../../model/production-status';

import { ProductionStatusService } from '../../service/production-status.service';
import { SessionService } from 'src/app/service/session.service';

import { I18n, CustomDatepickerI18n } from '../../app.component';

import {
  sampleRequiredValidator, specificationFixRequiredValidator, textureArrivalRequiredValidator,
  attachmentArrivalRequiredValidator, sewingInRequiredValidator, sewingInspectionRequiredValidator,
  inspectionRequiredValidator, shipRequiredValidator, distaArrivalRequiredValidator
} from './validator/production-status-validator.directive';

@Component({
  selector: 'app-production-status-modal',
  templateUrl: './production-status-modal.component.html',
  styleUrls: ['./production-status-modal.component.scss'],
  providers: [I18n, {
    provide: NgbDatepickerI18n,
    useClass: CustomDatepickerI18n
  }, { provide: NgbDateParserFormatter, useClass: NgbDateFRParserFormatter }]
})
export class ProductionStatusModalComponent implements OnInit {
  @Input() orderId: number;     // 発注ID
  @Input() orderNumber: number; // 発注No

  // 生産ステータスプルダウン値
  readonly statusList = [
    { id: ProductionStatusType.SAMPLE, name: 'サンプル' },
    { id: ProductionStatusType.SPECIFICATION_FIX, name: '仕様確定' },
    { id: ProductionStatusType.TEXTURE_ARRIVAL, name: '生地入荷' },
    { id: ProductionStatusType.ATTACHMENT_ARRIVAL, name: '付属入荷' },
    { id: ProductionStatusType.SEWING_IN, name: '縫製中' },
    { id: ProductionStatusType.SEW_INSPECTION, name: '縫製検品' },
    { id: ProductionStatusType.INSPECTION, name: '検品' },
    { id: ProductionStatusType.SHIP, name: 'SHIP' },
    { id: ProductionStatusType.DISTA_ARRIVAL, name: 'DISTA入荷' }
  ];

  // HTMLから参照される生産ステータスの定数値
  PRODUCTION_STATUS_TYPE = ProductionStatusType;

  overall_error_msg_code = '';  // エラーメッセージコード
  overall_susses_msg_code = ''; // 成功メッセージコード
  currentHistoryStatus: ProductionStatusType; // 選択中の履歴ステータス

  isBtnLock = false;    // ボタンロックフラグ
  submitted = false;    // 送信フラグ
  searchLoading = true; // 検索中フラグ（連続クリック防止用）
  isShowEdiButton = false; // 操作ボタン表示フラグ

  sampleCompletionList: ProductionStatusHistory[] = [];   // サンプルリスト
  specificationFixList: ProductionStatusHistory[] = [];   // 仕様確定リスト
  textureArrivalList: ProductionStatusHistory[] = [];     // 生地入荷リスト
  attachmentArrivalList: ProductionStatusHistory[] = [];  // 付属入荷リスト
  sewingInList: ProductionStatusHistory[] = [];           // 縫製中リスト
  sewInspectionList: ProductionStatusHistory[] = [];      // 縫製検品リスト
  inspectionList: ProductionStatusHistory[] = [];         // 検品リスト
  shipList: ProductionStatusHistory[] = [];               // shipリスト
  distaArrivalList: ProductionStatusHistory[] = [];       // dista入荷日リスト

  productionStatusForm: FormGroup;  // 入力フォーム
  productionStatus: ProductionStatus; // 最新の生産ステータス履歴情報

  constructor(
    private sessionService: SessionService,
    public activeModal: NgbActiveModal,
    public productionStatusService: ProductionStatusService,
    private formBuilder: FormBuilder,
    private ngbDateParserFormatter: NgbDateParserFormatter
  ) { }

  /**
   * formの項目の状態を取得する。
   * @return productionStatusForm.controls
   */
  get f(): any { return this.productionStatusForm.controls; }

  ngOnInit() {
    const session = this.sessionService.getSaveSession(); // ログインユーザの情報を取得する
    // 登録ボタン表示条件: ROLE_EDIまたはROLE_MAKER
    this.isShowEdiButton = AuthUtils.isEdi(session) || AuthUtils.isMaker(session);

    // 入力フォーム作成
    this.productionStatusForm = this.formBuilder.group({
      id: [null], // ID
      orderId: [this.orderId], // 発注ID
      orderNumber: [this.orderNumber], // 発注No
      productionStatusId: [null], // 生産ステータスID
      productionStatusType: [ProductionStatusType.SAMPLE], // 生産ステータス
      sampleCompletionAt: [null], // サンプル上がり予定日
      sampleCompletionFixAt: [null], // サンプル上がり確定予定日
      specificationAt: [null], // 仕様予定日
      specificationFixAt: [null], // 仕様確定日
      textureArrivalAt: [null], // 生地入荷予定日
      textureArrivalFixAt: [null], // 生地入荷確定日
      attachmentArrivalAt: [null], // 付属入荷予定日
      attachmentArrivalFixAt: [null], // 付属入荷確定日
      completionAt: [null], // 上がり予定日
      completionFixAt: [null], // 上がり予定確定日
      completionCount: [null, Validators.pattern(/^[0-9,]*$/)], // 上がり総数
      sewInspectionAt: [null], // 縫製検品到着予定日
      sewInspectionFixAt: [null], // 縫製検品到着確定日
      inspectionAt: [null], // 検品実施予定日
      inspectionFixAt: [null], // 検品実施確定日
      leavePortAt: [null], // 出港予定日
      leavePortFixAt: [null], // 出港確定日
      enterPortAt: [null], // 入港予定日
      enterPortFixAt: [null], // 入港確定日
      customsClearanceAt: [null], // 通関予定日
      customsClearanceFixAt: [null], // 通関確定日
      distaArrivalAt: [null], // DISTA入荷予定日
      distaArrivalFixAt: [null], // DISTA入荷確定日
      memo: [null]  // メモ
    });
    this.currentHistoryStatus = ProductionStatusType.SAMPLE;  // 現在は01:縫製検品のみなので、サンプルを履歴表示の初期値にする
    this.setInitData(this.orderId); // データの取得を行う。
    this.onChangeStatusSelect(ProductionStatusType.SAMPLE); // リスト取得後、デフォルトステータス表示
  }

  /**
    * 初期表示で必要なデータ取得して設定する。
    * @param orderId 発注ID
    * */
  private setInitData(orderId: number): void {
    this.searchLoading = true;
    this.getProductionStatus(orderId).then((productionStatusList) => {
      // 取得したデータの先頭を最新情報として保持しておく。
      if (productionStatusList.length > 0) {
        this.productionStatus = productionStatusList[0];
      }
      this.getProductionStatusHistory(orderId).then(productionStatusHistoryList => {
        // 履歴表示用にデータ編集を行う。
        this.setHistorysByStatus(productionStatusHistoryList);
        this.searchLoading = false;
      });
    });
  }


  /**
   * formの入力値を初期化する。
   */
  private resetFormValue(): void {
    this.productionStatusForm.reset({
      orderId: this.orderId, // 発注ID
      orderNumber: this.orderNumber, // 発注No
    });
  }

  /**
   * 生産ステータス登録処理
   */
  onSubmit(): void {
    this.isBtnLock = true;
    this.searchLoading = true;
    this.submitted = true;
    this.overall_susses_msg_code = '';
    this.overall_error_msg_code = '';
    if (this.productionStatusForm.invalid) {
      console.debug('バリデーションエラー', this.productionStatusForm);
      this.overall_error_msg_code = 'ERRORS.ANY_ERROR';
      this.searchLoading = false;
      this.isBtnLock = false;
      return;
    }

    this.productionStatusService.postProductionStatus(this.productionStatusForm.value).toPromise().then(
      () => {
        this.overall_susses_msg_code = 'SUCSESS.PRODUCTION_STATUS_ENTRY';
        this.submitted = false;
        this.isBtnLock = false;
        this.setInitData(this.orderId); // 履歴リストの再読み込み
      },
      error => { this.handleApiError(error); }
    );
  }

  /**
   * 生産ステータス情報取得
   * @param orderId 発注ID
   * @returns 成功時：取得データ、失敗時：エラー情報
   */
  private async getProductionStatus(orderId: number): Promise<ProductionStatus[]> {
    const searchCondition = { orderId: orderId } as ProductionStatusHistorySearchCondition;
    return await this.productionStatusService.getProductionStatusSearch(searchCondition).toPromise().then(
      result => {
        const productionStatusList = result.items;
        return Promise.resolve(productionStatusList);
      },
      error => {
        console.debug('getOrderData error:', error);
        this.handleApiError(error);
        return Promise.reject(error);
      }
    );
  }

  /**
   * 生産ステータス履歴情報取得
   * @param orderId 発注ID
   * @returns 成功時：取得データ、失敗時：エラー情報
   */
  private async getProductionStatusHistory(orderId: number): Promise<ProductionStatusHistory[]> {
    const searchCondition = { orderId: orderId } as ProductionStatusHistorySearchCondition;
    return await this.productionStatusService.getProductionStatusHistorySearch(searchCondition).toPromise().then(
      productionStatusHistorys => {
        const productionStatusHistoryList = productionStatusHistorys.items;
        return Promise.resolve(productionStatusHistoryList);
      },
      error => {
        this.handleApiError(error);
        return Promise.reject(error);
      }
    );
  }

  /**
   * 日付フォーカスアウト時処理
   * @param maskedValue 変換済の値
   * @param type 日付項目名
   */
  onBlurDate(maskedValue: string, type: string): void {
    // patchValueしないとpipeの変換値がformにセットされない
    const ngbDate = DateUtils.onBlurDate(maskedValue);
    if (ngbDate) {
      this.productionStatusForm.patchValue({ [type]: ngbDate });
    }
  }

  /**
   * APIエラー処理
   * @param error エラー情報
   */
  private handleApiError(error: any): void {
    const apiError = ExceptionUtils.apiErrorHandler(error);
    if (apiError != null) {
      let errorCode = '';
      if (apiError.viewErrors == null || apiError.viewErrors[0].viewErrorMessageCode == null) {
        // viewErrorMessageCodeがある(＝errorsがない、またはerrrosに表示用コードが設定されていない)
        errorCode = apiError.viewErrorMessageCode;
      } else {
        errorCode = apiError.viewErrors[0].viewErrorMessageCode;
      }
      this.overall_error_msg_code = errorCode;
    }
    this.submitted = false;
    this.searchLoading = false;
    this.isBtnLock = false;
  }

  /**
   * ステータス別履歴リスト作成
   */
  private setHistorysByStatus(allProductionStatusHistoryList: ProductionStatusHistory[]): void {
    // サンプルリスト
    this.sampleCompletionList = allProductionStatusHistoryList.filter(statusHistory => {
      return statusHistory.productionStatusType === ProductionStatusType.SAMPLE;
    });
    // 仕様確定リスト
    this.specificationFixList = allProductionStatusHistoryList.filter(statusHistory => {
      return statusHistory.productionStatusType === ProductionStatusType.SPECIFICATION_FIX;
    });
    // 生地入荷リスト
    this.textureArrivalList = allProductionStatusHistoryList.filter(statusHistory => {
      return statusHistory.productionStatusType === ProductionStatusType.TEXTURE_ARRIVAL;
    });
    // 付属入荷リスト
    this.attachmentArrivalList = allProductionStatusHistoryList.filter(statusHistory => {
      return statusHistory.productionStatusType === ProductionStatusType.ATTACHMENT_ARRIVAL;
    });
    // 縫製中リスト
    this.sewingInList = allProductionStatusHistoryList.filter(statusHistory => {
      return statusHistory.productionStatusType === ProductionStatusType.SEWING_IN;
    });
    // 縫製検品リスト
    this.sewInspectionList = allProductionStatusHistoryList.filter(statusHistory => {
      return statusHistory.productionStatusType === ProductionStatusType.SEW_INSPECTION;
    });
    // 検品リスト
    this.inspectionList = allProductionStatusHistoryList.filter(statusHistory => {
      return statusHistory.productionStatusType === ProductionStatusType.INSPECTION;
    });
    // shipリスト
    this.shipList = allProductionStatusHistoryList.filter(statusHistory => {
      return statusHistory.productionStatusType === ProductionStatusType.SHIP;
    });
    // dista入荷日リスト
    this.distaArrivalList = allProductionStatusHistoryList.filter(statusHistory => {
      return statusHistory.productionStatusType === ProductionStatusType.DISTA_ARRIVAL;
    });
  }

  /**
   * ステータスプルダウン切り替え時処理
   * @param status 生産ステータス
   */
  onChangeStatusSelect(status: ProductionStatusType): void {
    this.submitted = false;
    this.overall_susses_msg_code = '';
    this.overall_error_msg_code = '';

    // コントロールのバリデーション及び入力値を初期化
    // ※ngbDatepickerのバリデーションが効かなくなるので個別のバリデーションはクリアしないこと
    this.productionStatusForm.clearValidators();
    this.onChangeHistory(status);
    this.setFormInputControl(Number(status)); // htmlからの入力値はstringの為、型変換する
  }

  /**
   * フォームの入力項目に入力値、バリデーションを設定する。
   * 指定した生産ステータスの最新のレコードの値を設定する。
   * @param status 生産ステータス
   */
  private setFormInputControl(status: ProductionStatusType): void {
    let record: ProductionStatusHistory;
    this.resetFormValue();
    this.productionStatusForm.patchValue({
      productionStatusType: status, // 生産ステータス
    });
    switch (status) {
      // サンプル
      case ProductionStatusType.SAMPLE:
        this.productionStatusForm.setValidators(sampleRequiredValidator);
        record = this.sampleCompletionList[0];
        if (record) {
          this.productionStatusForm.patchValue({
            sampleCompletionAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.sampleCompletionAt)),
            sampleCompletionFixAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.sampleCompletionFixAt))
          });
        }
        break;
      // 仕様確定
      case ProductionStatusType.SPECIFICATION_FIX:
        this.productionStatusForm.setValidators(specificationFixRequiredValidator);
        record = this.specificationFixList[0];
        if (record) {
          this.productionStatusForm.patchValue({
            specificationAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.specificationAt)),
            specificationFixAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.specificationFixAt))
          });
        }
        break;
      // 生地入荷
      case ProductionStatusType.TEXTURE_ARRIVAL:
        this.productionStatusForm.setValidators(textureArrivalRequiredValidator);
        record = this.textureArrivalList[0];
        if (record) {
          this.productionStatusForm.patchValue({
            textureArrivalAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.textureArrivalAt)),
            textureArrivalFixAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.textureArrivalFixAt))
          });
        }
        break;
      // 付属入荷
      case ProductionStatusType.ATTACHMENT_ARRIVAL:
        this.productionStatusForm.setValidators(attachmentArrivalRequiredValidator);
        record = this.attachmentArrivalList[0];
        if (record) {
          this.productionStatusForm.patchValue({
            attachmentArrivalAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.attachmentArrivalAt)),
            attachmentArrivalFixAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.attachmentArrivalFixAt))
          });
        }
        break;
      // 縫製中
      case ProductionStatusType.SEWING_IN:
        this.productionStatusForm.setValidators(sewingInRequiredValidator);
        record = this.sewingInList[0];
        if (record) {
          this.productionStatusForm.patchValue({
            completionAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.completionAt)),
            completionFixAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.completionFixAt)),
            completionCount: record.completionCount
          });
        }
        break;
      // 縫製検品
      case ProductionStatusType.SEW_INSPECTION:
        this.productionStatusForm.setValidators(sewingInspectionRequiredValidator);
        record = this.sewInspectionList[0];
        if (record) {
          this.productionStatusForm.patchValue({
            sewInspectionAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.sewInspectionAt)),
            sewInspectionFixAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.sewInspectionFixAt))
          });
        }
        break;
      // 検品
      case ProductionStatusType.INSPECTION:
        this.productionStatusForm.setValidators(inspectionRequiredValidator);
        record = this.inspectionList[0];
        if (record) {
          this.productionStatusForm.patchValue({
            completionAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.completionAt)),
            inspectionAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.inspectionAt)),
            completionFixAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.completionFixAt)),
            inspectionFixAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.inspectionFixAt)),
            completionCount: record.completionCount
          });
        }
        break;
      // ship
      case ProductionStatusType.SHIP:
        this.productionStatusForm.setValidators(shipRequiredValidator);
        record = this.shipList[0];
        if (record) {
          this.productionStatusForm.patchValue({
            leavePortAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.leavePortAt)),
            enterPortAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.enterPortAt)),
            customsClearanceAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.customsClearanceAt)),
            leavePortFixAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.leavePortFixAt)),
            enterPortFixAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.enterPortFixAt)),
            customsClearanceFixAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.customsClearanceFixAt))
          });
        }
        break;
      // dista入荷日
      case ProductionStatusType.DISTA_ARRIVAL:
        this.productionStatusForm.setValidators(distaArrivalRequiredValidator);
        record = this.distaArrivalList[0];
        if (record) {
          this.productionStatusForm.patchValue({
            distaArrivalAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.distaArrivalAt)),
            distaArrivalFixAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(record.distaArrivalFixAt))
          });
        }
        break;
      default:
        break;
    }
  }

  /**
   * 履歴切り替え時処理
   * @param status 生産ステータス
   */
  onChangeHistory(status: ProductionStatusType): void {
    this.currentHistoryStatus = status;
  }
}
