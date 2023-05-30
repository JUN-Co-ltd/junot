// Angularコア機能の呼び出し定義1
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup } from '@angular/forms';
// ng-bootstrapモジュールの呼び出し定義1
import { NgbDateParserFormatter} from '@ng-bootstrap/ng-bootstrap';

import {
  AuthType, CompositionsCommon,
  Path, RegistStatus, OrderApprovalStatus, QualityApprovalStatus, ViewMode
} from '../../const/const';
import { CodeMaster } from '../../const/code-master';

// 共通関数の呼び出し
import { StringUtils } from '../../util/string-utils';
import { CalculationUtils } from '../../util/calculation-utils';
import { ExceptionUtils } from '../../util/exception-utils';
import { BusinessCheckUtils } from 'src/app/util/business-check-utils';
import { AuthUtils } from 'src/app/util/auth-utils';

// 使用するAPIに紐づくモデルの定義1
import { Order } from '../../model/order';
import { VOrder } from '../../model/v-order';
import { Item } from '../../model/item';
import { Sku } from '../../model/sku';
import { OrderSku } from '../../model/order-sku';
import { Compositions } from '../../model/compositions';
import { OrderViewCompositions } from '../../model/order-view-compositions';
import { Delivery } from '../../model/delivery';
import { Session } from 'src/app/model/session';

// 使用するAPIの定義1
import { SessionService } from '../../service/session.service';
import { OrderService } from '../../service/order.service';
import { ItemService } from '../../service/item.service';

// これはローディングに関する処理でAPIとは関係ない
import { LoadingService } from '../../service/loading.service';
// ヘッダーを表示するためのサービス
import { HeaderService } from '../../service/header.service';

// RxJS
import { Observable, forkJoin, of } from 'rxjs';
import { tap, flatMap, catchError, finalize } from 'rxjs/operators';

@Component({
  selector: 'app-order-approval', // htmlでコンポーネントを呼び出す際のセレクタ
  templateUrl: './order-approval.component.html',
  styleUrls: ['./order-approval.component.scss']
})
export class OrderApprovalComponent implements OnInit {

  // htmlから参照したい定数を定義
  readonly AUTH_INTERNAL: AuthType = AuthType.AUTH_INTERNAL; // JUN権限(社内)
  readonly REGIST_STATUS = RegistStatus;
  readonly PATH = Path;
  readonly VIEW_MODE = ViewMode;

  mainForm: FormGroup;  // メインのフォーム

  // 使用するAPIに紐づくモデルの定義2
  orderData: Order;                     // 発注情報のデータ
  itemData: Item;                       // 品番情報のデータ
  skusValue: Sku[];                     // 品番のSKU情報のデータ
  orderSkuValue: OrderSku[];            // 発注SKU情報のデータ
  deliveryList: Delivery[] = [];        // 納品依頼情報のデータ
  deliveryHistoryList: Delivery[] = []; // 納品履歴リスト

  compViewList: OrderViewCompositions[] = [];   // 組成情報のデータリスト
  seasonValue: string; // サブシーズンのvalue値
  seasonName: string;  // シーズン名
  orderAmount = 0;     // 金額
  qualityCompositionStatus = null;

  // 各種フラグ
  isConfirmed = false;      // 発注確定済み判断フラグ
  isInitDataSetted = false; // 画面用データ取得完了フラグ
  isOrderComplete = false;  // 完納済み判断フラグ
  isBtnLock = false;        // 登録/更新/削除処理中にボタンをロックするためのフラグ
  // PRD_0112 #7710 JFE add start
  isSeihin = false;         //発注区分(製品)判断フラグ
  // PRD_0112 #7710 JFE add end

  // ログインユーザ情報
  private session: Session;
  affiliation: AuthType;             // 所属
  accountName = '';                  // アカウント名
  orderApprovalAuthorityBlands = []; // 承認権限

  // 現在の画面のURLを取得
  path = ''; // approval

  // メッセージ
  overall_susses_msg_code = ''; // 画面全体にかかる正常系メッセージ
  overall_error_msg_code = '';  // 画面全体にかかる異常系メッセージ

  constructor(
    // Angularコア機能の呼び出し定義2
    private route: ActivatedRoute,
    private formBuilder: FormBuilder,

    // ng-bootstrapモジュールの呼び出し定義1
    private ngbDateParserFormatter: NgbDateParserFormatter,

    // 使用するAPIの定義2
    private sessionService: SessionService,
    private orderService: OrderService,
    private itemService: ItemService,

    // その他
    private loadingService: LoadingService,
    private headerService: HeaderService,
  ) {}

  // コンポーネントの初期処理
  ngOnInit() {
    // 共通ヘッダの表示
    this.headerService.show();
    // URLからパラメータを取得する
    const orderId: number = Number(this.route.snapshot.params['id']);    // 発注Id
    // URL末尾の文字列「approval」を取得する
    // ※app-sku-inputコンポーネントのフォーム非活性に必要なため
    this.path = this.route.snapshot.url[this.route.snapshot.url.length - 1].path;
    // ログインユーザの情報取得
    this.session = this.sessionService.getSaveSession();
    this.affiliation = this.session.affiliation;
    this.accountName = this.session.accountName;
    this.orderApprovalAuthorityBlands = this.sessionService.getSaveSession().orderApprovalAuthorityBlands;

    this.loadingService.loadStart();
    this.fetchData(orderId).pipe(
      // formに取得データセット
      tap(([order]) => this.setFormValue(order)),
      // 入力フォームをすべて非活性
      tap(() => this.mainForm.disable()),
      // フォームを表示
      tap(() => this.isInitDataSetted = true),
      catchError(err => {
        this.handleApiError(err);
        return of(null);
      }),
      finalize(() => this.loadingService.loadEnd())
    ).subscribe();
  }

  /**
   * データ取得処理.
   * @param orderId 発注ID
   * @returns Observable<取得データ>
   */
  private fetchData(orderId: number): Observable<[Order, Item]> {
    // 発注情報取得
    return this.fetchOrderData(orderId).pipe(
      // 品番情報取得
      flatMap(order => forkJoin(of(order), this.fetchItemData(order.partNoId)))
    );
  }

  /**
   * Formに発注情報を設定する.
   * @param orderData 発注情報データ
   */
  private setFormValue(orderData: Order): void {
    this.mainForm = this.getFromGroup();
    this.onSkusOrUnitPriceValueChanges();  // 発注SKUと原価の変更監視

    this.mainForm.patchValue({ id: orderData.id }); // 発注Id
    this.mainForm.patchValue({ orderNumber: orderData.orderNumber }); // 発注No
    this.mainForm.patchValue({ partNoId: orderData.partNoId }); // 品番ID
    this.mainForm.patchValue({ partNo: orderData.partNo }); // 品番
    this.mainForm.patchValue({ expenseItem: orderData.expenseItem }); // 費目
    this.mainForm.patchValue({ cutAutoType: orderData.cutAutoType }); // 自動裁断区分
    this.mainForm.patchValue({ matlMakerCode: orderData.matlMakerCode }); // 生地メーカー
    this.mainForm.patchValue({ matlPartNo: orderData.matlPartNo }); // 生地品番
    this.mainForm.patchValue({ matlProductName: orderData.matlProductName }); // 生地品名
    this.mainForm.patchValue({ matlDeliveryAt: orderData.matlDeliveryAt }); // 生地納期
    this.mainForm.patchValue({ matlMeter: orderData.matlMeter }); // 生地メーター数
    this.mainForm.patchValue({ matlUnitPrice: orderData.matlUnitPrice }); // 生地単価
    this.mainForm.patchValue({ clothNumber: orderData.clothNumber }); // 反番
    this.mainForm.patchValue({ necessaryLengthActual: orderData.necessaryLengthActual }); // 実用尺
    this.mainForm.patchValue({ necessaryLengthUnit: orderData.necessaryLengthUnit }); // 用尺単位
    this.mainForm.patchValue({ matlCost: orderData.matlCost }); // 生地原価
    this.mainForm.patchValue({ mdfMakerCode: orderData.mdfMakerCode }); // 生産メーカーコード
    this.mainForm.patchValue({  // 製品発注日
      productOrderAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(orderData.productOrderAt))
    });
    this.mainForm.patchValue({  // 製品修正納期
      productCorrectionDeliveryAt: this.ngbDateParserFormatter.parse(StringUtils.toStringSafe(orderData.productCorrectionDeliveryAt))
    });
    this.mainForm.patchValue({ mdfStaffCode: orderData.mdfStaffCode }); // 製造担当コード
    this.mainForm.patchValue({ productCompleteOrder: orderData.productCompleteOrder }); // 製品完納区分
    this.mainForm.patchValue({ quantity: orderData.quantity }); // 数量
    this.mainForm.patchValue({ unitPrice: orderData.unitPrice }); // 単価
    this.mainForm.patchValue({ retailPrice: orderData.retailPrice }); // 上代
    this.mainForm.patchValue({ productCost: orderData.productCost }); // 製品原価
    this.mainForm.patchValue({ processingCost: orderData.processingCost });  // 加工賃
    this.mainForm.patchValue({ attachedCost: orderData.attachedCost }); // 附属代
    this.mainForm.patchValue({ otherCost: orderData.otherCost }); // その他原価
    // PRD_0023 add JFE start
    this.mainForm.patchValue({ totalCost: null }); // 原価合計
    // PRD_0023 add JFE end
    this.mainForm.patchValue({ importCode: orderData.importCode }); // 輸入区分
    this.mainForm.patchValue({ application: orderData.application }); // 摘要
    this.mainForm.patchValue({ orderApproveStatus: orderData.orderApproveStatus }); // 発注承認ステータス

    this.mainForm.patchValue({
      cooCode: orderData.cooCode, // 原産国コード
      cooName: orderData.cooName, // 原産国名称
      mdfMakerCode: orderData.mdfMakerCode, // 生産メーカーコード
      mdfMakerName: orderData.mdfMakerName, // 生産メーカー名
      consignmentFactory: orderData.consignmentFactory, // 委託先工場
    });
  }

  /**
   * メインのFormGroupを返す
   * @return FormGroup
   */
  private getFromGroup(): FormGroup {
    return this.formBuilder.group({
      id: [null], // 発注Id
      orderNumber: [null],  // 発注No
      partNoId: [null], // 品番ID
      partNo: [null], // 品番
      expenseItem: ['01'],  // 費目
      cutAutoType: [null],  // 自動裁断区分
      matlMakerCode: [null],  // 生地メーカー
      matlPartNo: [null], // 生地品番
      matlProductName: [null],  // 生地品名
      matlDeliveryAt: [null], // 生地納期
      matlMeter: [null],  // 生地メーター数
      matlUnitPrice: [null],  // 生地単価
      clothNumber: [null],  // 反番
      necessaryLengthActual: [null],  // 実用尺
      necessaryLengthUnit: [null],  // 用尺単位
      matlCost: [null], // 生地原価
      mdfMakerCode: [null], // 生産メーカー
      mdfMakerName: [null], // 生産メーカー名
      mdfMakerFactoryCode: [null],  // 生産工場コード
      mdfMakerFactoryName: [null],  // 生産工場名
      consignmentFactory: [null], // 委託先工場
      productOrderAt: [null], // 製品発注日
      productCorrectionDeliveryAt: [null],  // 製品修正納期
      mdfStaffCode: [null], // 製造担当コード
      productCompleteOrder: [null], // 製品完納区分
      quantity: [null], // 数量
      unitPrice: [null],  // 単価
      retailPrice: [null],  // 上代
      productCost: [null],  // 製品原価
      processingCost: [null], // 加工賃
      attachedCost: [null], // 附属代
      otherCost: [null],  // その他原価
      // PRD_0023 add JFE start
      totalCost: [null], // 原価合計
      // PRD_0023 add JFE end
      importCode: [null], // 輸入区分
      cooCode: [null],  // 原産国コード
      cooName: [null], // 原産国名称
      application: [null],  // 摘要
      orderApproveStatus: [null],  // 発注承認ステータス
      orderSkus: this.formBuilder.array([]),  // 発注SKU情報
      costRate: [null], // 原価率(バリデーションで必要)
      orderFileInfo: [null] // 発注ファイル情報
    });
  }

  /**
   * 発注総数を合計して返す
   * @returns 発注総数
   */
  sumAllOrderQuantity(): number {
    const orderSkus = this.orderService.convertSku(this.mainForm.getRawValue().orderSkus);
    const qantity = this.orderService.sumAllOrderQuantity(orderSkus);
    this.mainForm.patchValue({ quantity: qantity });  // formに数量を登録
    return qantity;
  }

  // PRD_0023 mod JFE start
  /**
   * 発注金額を合計して返す.
   * 原価 × 数量
   * @returns 発注金額
   */
  private calculateOrderAmount(): number {
    const totalCost = this.calculateTotal().toString();
    const quantity = this.sumAllOrderQuantity();

    let orderAmount = 0;
    if (StringUtils.isNotEmpty(totalCost) && quantity != null) {
      orderAmount = +totalCost * quantity;
    }
    return orderAmount;
  }

  /**
   * 原価率計算
   * @returns 原価率
   */
  calculateCostRate(): number {
    const rate = CalculationUtils.calcRate(this.calculateTotal().toString(), this.mainForm.controls.retailPrice.value);
    this.mainForm.patchValue({ costRate: rate });
    return rate;
  }

  // PRD_0023 mod JFE end
  // PRD_0023 add JFE start
    /**
   * 原価合計.
   * @returns 原価合計
   */
    calculateTotal(): number {
      if (this.mainForm.controls) {
        const matlCost: number = this.mainForm.controls.matlCost.value === undefined ? 0 : this.mainForm.controls.matlCost.value;
        const processingCost: number = this.mainForm.controls.processingCost.value === undefined ?
          0 : this.mainForm.controls.processingCost.value;
        const assrCost: number = this.mainForm.controls.attachedCost.value === undefined ? 0 :
          this.mainForm.controls.attachedCost.value;
        const otherCost: number = this.mainForm.controls.otherCost.value === undefined ? 0 : this.mainForm.controls.otherCost.value;
        const totalCost: number = +matlCost + +processingCost + +assrCost + +otherCost;
        return totalCost;
      }
      return 0;
    }
  // PRD_0023 add JFE end

  /**
   * 発注残数計算
   * @returns 発注残数
   */
  remainingOrder(): number {
    // 発注残数 = 発注数 - ( 納品済数 - 返品数 )
    return this.orderData.quantity - this.sumLotFromHistoryList() - this.sumReturnLot(this.orderData.orderSkus);
  }

  /**
   * 全ての過去納品リストから納品数を全て合計して返す。
   * 仕入が確定している(納品明細の入荷フラグがtrue)場合は入荷数量を足す。
   * 未確定の場合は納品依頼数を足す。
   * @returns 納品数量合計
   */
  private sumLotFromHistoryList(): number {
    let allLot = 0;
    if (this.deliveryHistoryList) {
      this.deliveryHistoryList.forEach(history =>
        history.deliveryDetails.forEach(detail =>
          detail.deliverySkus.forEach(sku => allLot += (detail.arrivalFlg ? sku.arrivalLot : sku.deliveryLot))));
    }
    return allLot;
  }

  /**
   * 返品数を全て合計して返す。
   * @param orderSkus 発注SKU情報リスト
   * @returns 返品数合計
   */
  private sumReturnLot(orderSkus: OrderSku[]): number {
    let returnLotSum = 0;
    orderSkus.forEach(sku => returnLotSum += sku.returnLot);
    return returnLotSum;
  }

  /**
   * ログインユーザに発注の承認権限があるか判断する
   * @returns 権限があればtrue
   */
  isApproveableUser(): boolean {
    // ROLE_EDIでない場合はfalse
    if (!AuthUtils.isEdi(this.session)) { return false; }
    // 空の場合は無条件でfalse
    if (this.orderApprovalAuthorityBlands == null) { return false; }
    // ユーザが持つ承認権限の一覧に、発注の品番に紐づくブランドコードがあるor全権限コード"ZZ"がある場合はtrue
    return this.orderApprovalAuthorityBlands.includes(this.itemData.brandCode)
      || this.orderApprovalAuthorityBlands.includes('ZZ') ? true : false;
  }

  /**
   * 組成情報の作成
   * @param compositionList 品番の組成情報
   */
  private creatCompositionsSkuList(compositionList: Compositions[]): void {
    this.compViewList = [];

    // 組成情報を基に色のリストを作成する。
    if (compositionList !== null) {
      // 抽出した色コード、色名リストから色コードの重複除去
      const uniqueCompositionList = compositionList.filter(
        (value1, idx, array) => (array.findIndex(value2 => value2.colorCode === value1.colorCode) === idx)
      );
      uniqueCompositionList.forEach(uniqueComposition => {
        // 共通の場合はラベルをセットする。
        if (uniqueComposition.colorCode === CompositionsCommon.COLOR_CODE) {
          uniqueComposition.colorName = CompositionsCommon.COLOR_NAME;
        }

        // この色の組成情報を抽出する
        if (compositionList != null) {
          const compositions = compositionList.filter(composition => uniqueComposition.colorCode === composition.colorCode);
          if (compositions.length > 0) {
            const orderViewCompositions = uniqueComposition as OrderViewCompositions;
            orderViewCompositions.compositions = compositions;
            this.compViewList.push(orderViewCompositions);
          }
        }
      });
    }
  }

  /**
   * 季節名を設定する.
   * @param seasonCode シーズンコード
   */
  private setSeasonName(seasonCode: string): void {
    const seasonCodeStr = String(seasonCode);
    const subSeason = CodeMaster.seasonName.find(x => x.code === seasonCodeStr);
    this.seasonName = subSeason.value;
  }

  /**
   * サブシーズンのコード名を設定する.
   * @param subSeasonCode サブシーズンコード
   */
  private setSeasonValue(subSeasonCode: string): void {
    const subSeasonId = Number(subSeasonCode);
    const subSeason = CodeMaster.subSeason.find(x => x.id === subSeasonId);
    this.seasonValue = subSeason.value;
  }

  /**
   * 発注確定済みか判断する。
   * @param orderData 発注情報
   */
  private isOrderConfirmed(orderData: Order): void {
    const vOrder = { orderApproveStatus: orderData.orderApproveStatus } as VOrder;
    this.isConfirmed = BusinessCheckUtils.isConfirmOrderOk(vOrder);
  }

  // PRD_0023 mod JFE start
  /**
   * 発注情報SKUと原価のデータ変更監視
   */
  private onSkusOrUnitPriceValueChanges(): void {
    // 発注情報SKUの変更監視
    this.mainForm.get('orderSkus').valueChanges.subscribe(() => {
      // 発注SKUが変更されたら金額の再集計を行う。
      this.orderAmount = this.calculateOrderAmount();
    });
    // 原価の状態監視
    this.mainForm.get('totalCost').valueChanges.subscribe(() => {
      // 原価が変更されたら金額の再集計を行う。
      this.orderAmount = this.calculateOrderAmount();
    });
  }
  // PRD_0023 mod JFE end

  /**
   * サブメニューリンククリック時の処理.
   * ページ内遷移.
   * @param id HTML要素のID
   * @returns false
   */
  scrollEvent(id: string): boolean {
    window.scrollTo(0,
      window.pageYOffset
      + document.getElementById(id).getBoundingClientRect().top
      - document.getElementById('header').getBoundingClientRect().height
    );
    return false;
  }

  /**
   * 発注承認ステータスラベルの表示文言を返却
   * @returns 発注承認ステータスラベル文字
   */
  orderStatusText(): string {
    let label: string = null;

    if (this.orderData != null) {
      switch (this.orderData.orderApproveStatus) {
        case OrderApprovalStatus.ACCEPT:
          label = '発注承認済';
          break;
        case OrderApprovalStatus.CONFIRM:
        case OrderApprovalStatus.REJECT:
          label = '発注未承認';
          break;
        case OrderApprovalStatus.UNAPPROVED:
        case OrderApprovalStatus.CONFIRM_REJECT:
          label = '発注未確定';
          break;
        default:
          label = '発注未確定';
          break;
      }
    }
    return label;
  }

  /**
   * 発注ステータスが承認可能か判断する
   * @returns 承認可能ならtrue
   */
  isApproveableStatus(): boolean {
    let status = false;
    if (this.orderData != null) {
      switch (this.orderData.orderApproveStatus) {
        case OrderApprovalStatus.CONFIRM:
        case OrderApprovalStatus.REJECT:
          status = true;
          break;
        default:
          break;
      }
    }
    return status;
  }

  /**
   * 発注承認ステータスラベルの表示ステータスを返却
   * @returns 表示ステータス(クラス名)
   */
  showOrderStatusClass(): string {
    let label: string = null;

    if (this.orderData != null) {
      switch (this.orderData.orderApproveStatus) {
        case OrderApprovalStatus.UNAPPROVED:
        case OrderApprovalStatus.CONFIRM_REJECT:
        case OrderApprovalStatus.ACCEPT:
          label = 'confirm-approve-lable';
          break;
        case OrderApprovalStatus.CONFIRM:
        case OrderApprovalStatus.REJECT:
          label = 'unapproved-lable';
          break;
        default:
          break;
      }
    }
    return label;
  }

  /**
   * 優良誤認ステータスラベルの表示ステータスを返却
   * @param qualityApprovalStatus 優良誤認承認ステータス
   * @returns 表示ステータス(クラス名)
   */
  showQualityStatusClass(qualityApprovalStatus: number): string {
    let label: string = null;
    if (qualityApprovalStatus != null) {
      switch (qualityApprovalStatus) {
        case QualityApprovalStatus.TARGET:
        case QualityApprovalStatus.PART:
          label = 'badge-danger';
          break;
        case QualityApprovalStatus.ACCEPT:
          label = 'badge-primary';
          break;
        default:
          break;
      }
    }
    return label;
  }

  /**
   * 優良誤認承認のステータスラベル表示文言を返却
   * @param qualityApprovalStatus 優良誤認承認ステータス
   * @returns ステータスラベル文字
   */
  showQualityApprovalStatuslabel(qualityApprovalStatus: number): string {
    let label: string = null;
    if (qualityApprovalStatus != null) {
      switch (qualityApprovalStatus) {
        case QualityApprovalStatus.TARGET:
        case QualityApprovalStatus.PART:
          label = '未承認';
          break;
        case QualityApprovalStatus.ACCEPT:
          label = '承認済';
          break;
        default:
          break;
      }
    }
    return label;
  }

  /**
   * 発注情報取得処理
   * @param orderId 発注ID
   * @returns 発注情報
   */
  private fetchOrderData(orderId: number): Observable<Order> {
    return this.orderService.getOrderForId(orderId).pipe(
      tap(order => this.orderData = order),
      tap(order => this.orderSkuValue = order.orderSkus),
      // 発注確定済みかチェック
      tap(order => this.isOrderConfirmed(order))
    );
  }

  /**
   * 品番情報取得処理
   * @param partNoId 品番ID
   * @param orderData 発注情報
   */
  private fetchItemData(partNoId: number): Observable<Item> {
    return this.itemService.getItemForId(partNoId).pipe(
      tap(item => this.itemData = item),
      tap(item => this.skusValue = item.skus),
      // 組成情報の作成
      tap(item => this.creatCompositionsSkuList(item.compositions)),
      // シーズンのvalue値を設定
      tap(item => this.setSeasonName(item.seasonCode)),
      tap(item => this.setSeasonValue(item.subSeasonCode)),
      tap(item => this.qualityCompositionStatus = item.qualityCompositionStatus)
    );
  }

  /**
   * 発注情報承認処理
   */
  onApproval(): void {
    this.loadingService.loadStart();
    this.clearErrorMessageWhenSubmit();
    //  PRD_0114 単価０NG対応#7820 --add JFE Start//
    let chkValue = this.orderData.unitPrice
    if (chkValue == 0 || chkValue == null) {
      //trueだったら単価０エラー
      this.loadingService.loadEnd();
      this.isBtnLock = false;
      this.overall_error_msg_code = 'ERRORS.VALIDATE.UNIT_PRICE_EMPTY';
      return;
    }
    //  PRD_0114 単価０NG対応#7820 --add JFE END--//
    const formValue = this.mainForm.getRawValue();
    this.orderService.approveOrder(formValue).pipe(
      flatMap(() => this.fetchOrderData(formValue.id)),
      tap(() => this.overall_susses_msg_code = 'SUCSESS.ORDER_APPROVED'),
      catchError(err => {
        this.handleApiError(err);
        return of(null);
      }),
      finalize(() => this.loadingService.loadEnd())
    ).subscribe();
  }

  /**
   * submit時にエラーメッセージをクリアする
   */
  private clearErrorMessageWhenSubmit(): void {
    this.overall_error_msg_code = '';
    this.overall_susses_msg_code = '';
    ExceptionUtils.clearErrorInfo();
  }

  /**
   * APIエラー処理
   * @param error APIレスポンスエラー情報
   */
  private handleApiError(error: any): void {
    const apiError = ExceptionUtils.apiErrorHandler(error);
    if (apiError != null && apiError.viewErrors != null) {
      const firstError = apiError.viewErrors[0];
      if (firstError != null) {
        switch (firstError.code) {
          // 画面全体の描画を更新しないエラー
          case '400_O_09': // 承認可能なステータスでない場合
            this.overall_error_msg_code = 'ERRORS.400_O_09';
            break;
          case '400_05': // ログインユーザに承認権限がない場合
            this.overall_error_msg_code = 'ERRORS.400_05';
            break;
          // 画面全体の描画を更新するエラー
          case '400_02': // 発注情報に紐づく品番が存在しない場合
            this.isInitDataSetted = false;
            // サーバエラーエリアにメッセージ表示
            ExceptionUtils.displayErrorInfo('noDataErrorInfo', 'ERRORS.400_02');
            break;
          case '400_12':
            this.overall_error_msg_code = 'ERRORS.400_12';
            break;
          default:
            this.overall_error_msg_code = 'ERRORS.ANY_ERROR';
            break;
        }
      }
    }
  }
}
