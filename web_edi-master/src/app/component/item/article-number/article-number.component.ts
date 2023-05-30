import { Component, OnInit, Input, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, FormControl, Validators, AbstractControl } from '@angular/forms';

import { Subscription } from 'rxjs';
import { tap, catchError, finalize} from 'rxjs/operators';

import { RegistStatus, JanType, ValidatorsPattern } from '../../../const/const';
import { ListUtils } from '../../../util/list-utils';
import { StringUtils } from '../../../util/string-utils';
import { ObjectUtils } from '../../../util/object-utils';
import { FormUtils } from '../../../util/form-utils';

import { ItemService } from '../../../service/item.service';
import { ItemDataService } from '../../../service/shared/item-data.service';
import { LoadingService } from '../../../service/loading.service';
import { MessageConfirmModalService } from 'src/app/service/message-confirm-modal.service';

import { Sku } from '../../../model/sku';
import { ErrorDetail } from '../../../model/error-detail';
import { JunpcSizmst } from '../../../model/junpc-sizmst';

import { articleNumbersValidator } from './validator/article-numbers-validator';
import { SkuFormValue } from 'src/app/interface/sku-form-value';
import { SizeFormValue } from 'src/app/interface/size-form-value';

class JanError {
  code: string;
  arg: string;
}

@Component({
  selector: 'app-article-number',
  templateUrl: './article-number.component.html',
  styleUrls: ['./article-number.component.scss'],
})
export class ArticleNumberComponent implements OnInit, OnDestroy {
  // htmlから参照したい定数を定義
  /** 定数：JAN区分 */
  readonly JAN_TYPE = JanType;

  /** 親画面のForm */
  @Input() parentForm: FormGroup = null;
  /** 親画面で編集中のデータの登録ステータス */
  @Input() private registStatus = RegistStatus.ITEM;
  /** 親画面でsubmitしたか */
  @Input() private submitted = false;
  /** コピー新規作成中か */
  @Input() private isCopy = false;

  /** JANフォーム作成時のSubscription */
  private createArticleNumbersFormSubscription: Subscription;
  /** JANフォーム行数変更時のSubscription(SKUの色コード変更あり) */
  private changeArticleNumbersRowSubscription: Subscription;
  /** JANフォーム非活性制御時のSubscription(SKUのチェック変更あり) */
  private changeJanCodeDisableSubscription: Subscription;
  /** JAN区分のSubscription(初期表示、JAN区分変更時) */
  private janTypeSubscription: Subscription;

  /** 変更前のJAN区分 */
  private preJanType: number = null;

  /** SKUフォーム作成完了フラグ */
  isSkuFormCreated = false;
  /** JAN/UPC入力フォーム表示フラグ */
  isShowArticleNumbers = false;
  /** 自社JAN自動登録判断フラグ */
  isAutomaticallyRegistInHouseJan = false;
  /** JAN/UPC折り畳みフラグ(true：折り畳む、false：折り畳まない) */
  isArticleNumberCollapsed = true;

  /** 画面に表示するJAN/UPCエラーメッセージ */
  articleNumberErrorMsg: JanError[] = [];




  /**
   * 親より渡されたのサイズマスタリスト.
   * @return this.itemDataService.sizeMasterList$
   */
  get sizeMasterList(): JunpcSizmst[] {
    return this.itemDataService.sizeMasterList;
  }

  /**
   * 親より渡されたJAN区分(画面上で選択したJAN区分).
   * @return this.itemDataService.janType$.value
   */
  get selectedJanType(): number {
    return this.itemDataService.janType$.value;
  }


  constructor(
    private formBuilder: FormBuilder,
    private loadingService: LoadingService,
    private itemDataService: ItemDataService,
    private itemService: ItemService,
    private messageConfirmModalService: MessageConfirmModalService
  ) {}

  /**
   * parentFormのarticleNumbersFormArrayを取得する.
   * @return this.parentForm.get('articleNumbers')
   */
  get articleNumbersFormArray(): FormArray {
    return this.parentForm.get('articleNumbers') as FormArray;
  }

  /**
   * parentFormのJAN/UPC項目の状態を取得する。
   * @return this.parentForm.get('articleNumbers').controls
   */
  get fCtrlArticleNumbers(): AbstractControl[] {
    return this.articleNumbersFormArray.controls;
  }

  /**
   * parentFormのskus項目の状態を取得する。
   * @return this.parentForm.get('skus').controls
   */
  get fCtrlSkus(): AbstractControl[] {
    return (<FormArray> this.parentForm.get('skus')).controls;
  }

  /**
   * parentFormのskusのvalueを返す.
   * @return this.parentForm.get('skus').value
   */
  get fValSkus(): SkuFormValue[] {
    return this.parentForm.get('skus').value;
  }

  ngOnInit() {
    let loadingToken = null;
    let isError = false;

    // ローディング開始
    this.loadingService.start().pipe(
      // ローディングトークン待避
      tap((token) => loadingToken = token),
      tap(() => {
        // 処理追加
        this.createArticleNumberFormAfterSkuFormCreated();
        // SKUで色コード変更時のarticleNumbers行変更
        this.changeArticleNumbersRowByColorInput();
        // SKU選択時のJAN/UPCコード入力ボックス非活性制御
        this.changeArticleNumberDisable();
        // 親画面でJAN区分が変更された場合のJAN/UPCフォーム制御
        this.janTypeChangeArticleNumbers();
      }),
      catchError((error) => {
        isError = true;
        return this.messageConfirmModalService.openErrorModal(error);
      }),
      finalize(() => {
        // 画面表示（エラーが発生した場合は、非表示のままとする）
        if (isError) {
          this.noShowArticleNumberNotExistSku();
        }
        // ローディング停止
        this.loadingService.stop(loadingToken);
      })
    ).subscribe();
  }

  ngOnDestroy() {
    // JAN/UPCコード入力フォームクリア(商品情報画面へ遷移)
    this.resetArticleNumberFormArray();
    // component destroyed時のメモリリーク防止
    this.createArticleNumbersFormSubscription.unsubscribe();
    this.changeArticleNumbersRowSubscription.unsubscribe();
    this.changeJanCodeDisableSubscription.unsubscribe();
    this.janTypeSubscription.unsubscribe();
  }

  /**
   * JAN/UPC折り畳みを展開するか.
   * ・JAN/UPC折り畳みフラグがfalse
   * または
   * ・JAN/UPCエラー時は折り畳みを展開する
   * @returns 展開する：true、展開しない：false
   */
  get isExpandArticleNumbers(): boolean {
    // JAN/UPCエラーがある場合は折り畳みを展開
    if (this.articleNumbersFormArray.invalid) {
      this.isArticleNumberCollapsed = false;
      return true;
    }
    // JAN/UPCエラーなし：
    // JAN/UPC折り畳みフラグがfalseの場合は折り畳みを展開
    if (!this.isArticleNumberCollapsed) {
      return true;
    }
    // 上記以外は展開しない
    return false;
  }

  /**
   * parentFormのArticleNumberFormをリセットする.
   * ※asyncValidator(articleNumbersValidator)再付与
   */
  private resetArticleNumberFormArray(): void {
    this.parentForm.setControl('articleNumbers', this.formBuilder.array([], null,
      articleNumbersValidator(this.itemService, this.selectedJanType)));
  }

  /**
   * JAN/UPCフォームを作成する。
   * ※以下処理をした際に、画面上で入力されているSKUを使い、articleNumberFormを作成
   * ・初期表示時
   * ・品種変更時
   * ・JAN区分変更時
   *
   * skuFormで色コード入力済みである場合は、チェック状態によりJAN/UPCフォームの活性状態を新しいarticleNumberFormに設定して作成する(入力値は保持しない)。
   * ※新たに入力された品種のサイズマスタにないサイズは非活性。
   */
  private createArticleNumberForm(): void {
    const inputtedSkus = this.getInputtedSkus();  // 既存のskuFormから色コードが入力されているskuリストを取得する。
    this.resetArticleNumberFormArray(); // 既存のarticleNumberFormクリア(クリアしないと既存のarticleNumberFormに新たに追加される)

    // 既存のskuFormに色コード入力済みである場合、articleNumberFormに設定して作成。
    // (ブランド変更前のSKU入力状態をブランド変更後も引き継ぐ)

    if (ListUtils.isEmpty(inputtedSkus)) {
      // SKUがない場合はJAN/UPCコード入力フォーム表示しない
      this.noShowArticleNumberNotExistSku();
      return;
    }

    this.createArticleNumberFormArraySettedValue(inputtedSkus, this.sizeMasterList);
    // JAN/UPC入力フォームの表示制御
    this.showArticleNumbersForm(this.selectedJanType);
  }

  /**
   * 既存のskuFormから色コードが入力されているskuリストを取得する。
   * (品種を変更しても前の色コード、サイズ、活性状況を引き継ぐ為 ※JAN/UPC入力値は保持しない)
   * @returns skuリスト
   */
  private getInputtedSkus(): Sku[] {
    const inputtedSkus: Sku[] = [];
    this.fValSkus.forEach(skuFormValue => {
      if (StringUtils.isNotEmpty(skuFormValue.colorCode)) {
        // 選択されているサイズリスト取得
        const selectedSizeList = skuFormValue.sizeList.filter(size => size.select);
        if (ListUtils.isNotEmpty(selectedSizeList)) {
          // 選択されているサイズのsku情報を戻り値のリストに格納
          selectedSizeList.forEach(sizeFormValue => {
            const sku = this.createSkuSettedValue(skuFormValue, sizeFormValue);
            inputtedSkus.push(sku);
          });
        } else {
          // 選択されているサイズが1つもなければ色コード・色名だけ設定したskuを戻り値のリストに格納
          const sku = this.createSkuSettedValue(skuFormValue);
          inputtedSkus.push(sku);
        }
      }
    });
    return inputtedSkus;
  }

  /**
   * 値をセットしたSku作成.
   * @param sku
   * @param size
   * @returns 値をセットしたSku
   */
  private createSkuSettedValue(sku: SkuFormValue, size?: SizeFormValue): Sku {
    const skuSettedValue = new Sku();
    skuSettedValue.colorCode = sku.colorCode;
    skuSettedValue.colorName = sku.colorName;
    skuSettedValue.select = false; // 未選択
    skuSettedValue.rowIndex = sku.rowIndex;
    if (!ObjectUtils.isNullOrUndefined(size)) {
      skuSettedValue.id = size.id; // ※DBから取得したsku情報でなければnull
      skuSettedValue.size = size.size;
      skuSettedValue.select = true; // 選択
      skuSettedValue.janCode = size.janCode; // JANコード値 ※品種変更時、JAN区分変更時はnull
    }
    return skuSettedValue;
  }

  /**
   * sku(色・コード)情報の値を設定したJAN/UPCのフォーム配列を作成する。(色・サイズのForm全行全列)
   * 未選択のSKUの場合、JANコード入力欄は非活性にする
   * @param allSkuValues formに設定するsku(色・コード)情報リスト
   * @param sizeMasterList サイズマスタ情報リスト
   */
  private createArticleNumberFormArraySettedValue(allSkuValues: Sku[], sizeMasterList: JunpcSizmst[]): void {
    // sku(色・コード)リストからSKU行番号の重複除去(色コード、色名称、SKU行番号が同じ)
    const uniqueSkusValue = allSkuValues.filter((sku1, idx, array) =>
      (array.findIndex(sku2 => sku2.rowIndex === sku1.rowIndex) === idx));

    // SKU行番号ごとにsku(色・コード・JAN)form作成
    uniqueSkusValue.forEach(skuValue => {
      // sku(色・コード)情報リストから処理中のSKU行番号に該当するデータを抽出する。
      const sameCodeSkuValues = allSkuValues.filter(sku => skuValue.rowIndex === sku.rowIndex);
      // skuFormGroup作成(サイズのFormArrayに色コード、色名、バリデーションを加えたform)
      const skuFormGroup = this.createSkuFormGroupForm(sizeMasterList, skuValue);
      // サイズのFormArray作成(サイズの入力ボックス1行全列)
      const sizeFormArray = this.createArticleNumberSizeFormArraySettedValue(sameCodeSkuValues, sizeMasterList);
      skuFormGroup.setControl('sizeList', sizeFormArray);
      this.articleNumbersFormArray.push(skuFormGroup);
    });
  }

  /**
   * sku(色・コード)情報の値を設定したJAN/UPCコードのサイズのform配列を作成して返す。(色・サイズの1行の中の全列)
   * 引数のskuリストに存在するサイズでもサイズマスタに存在しなければ作成しない。
   * @param sameCodeSkuValues formに設定するsku(色・コード)情報リスト※リスト内のデータの色コードは全て同じ
   * @param sizeMasterList サイズマスタ情報リスト
   * @returns サイズのフォーム配列
   */
  private createArticleNumberSizeFormArraySettedValue(sameCodeSkuValues: Sku[], sizeMasterList: JunpcSizmst[]): FormArray {
    const sizeFormArray = this.formBuilder.array([]);
    // 取得したサイズマスタ分チェックボックスform作成(1行の中の全列作成)
    sizeMasterList.forEach(sizeMaster => {
      // 1列デフォルトformGroup作成
      const oneSizeFormGroup = this.createSizeGroup(sizeMaster.szkg, sameCodeSkuValues[0].colorCode);

      // sku(色・コード)データ設定
      const sameSku = sameCodeSkuValues.find(skuValue => sizeMaster.szkg === skuValue.size);
      if (!ObjectUtils.isNullOrUndefined(sameSku)) {
        oneSizeFormGroup.patchValue({
          // ID設定。コピー新規の場合は新規採番する為null
          id: this.isCopy ? null : sameSku.id,
          // JANコード設定。コピー新規の場合は新規採番する為null
          janCode: this.isCopy ? null : sameSku.janCode,
          // DB登録済のJANコード設定。コピー新規の場合は新規採番する為null
          registedJanCode: this.isCopy ? null : sameSku.janCode,
          // SKUサイズ選択済み設定。
          select: true
        });
        // 自社JANの場合は入力フォーム非活性
        if (this.selectedJanType === JanType.IN_HOUSE_JAN && this.registStatus === RegistStatus.PART) {
          oneSizeFormGroup.disable();
        }
      } else {
        oneSizeFormGroup.disable();
      }
      sizeFormArray.push(oneSizeFormGroup);
    });
    return sizeFormArray;
  }

  /**
   * sku(色・サイズ)のフォームグループを作成して返す。(色・サイズの1行).
   * @param sizeMasterList サイズマスタ情報リスト
   * @param skuValue sku(色コード)情報
   */
  private createSkuFormGroupForm(sizeMasterList: JunpcSizmst[], skuValue: Sku): FormGroup {
    const skuFormGroup = this.formBuilder.group({
      colorCode: skuValue.colorCode,
      colorName: skuValue.colorName,
      sizeList: this.createSizeFormArray(sizeMasterList),
      rowIndex: skuValue.rowIndex
    });
    return skuFormGroup;
  }

  /**
   * サイズのフォーム配列を作成して返す。(色・サイズの1行の中の全列).
   * @param sizeMasterList サイズマスタ情報リスト
   * @returns サイズのフォーム配列
   */
  private createSizeFormArray(sizeMasterList: JunpcSizmst[]): FormArray {
    const sizeFormArray = this.formBuilder.array([]);
    for (let i = 0; i < sizeMasterList.length; i++) {
      const sizeFormGroup: FormGroup = this.createSizeGroup(sizeMasterList[i].szkg);
      sizeFormArray.push(sizeFormGroup);
    }
    return sizeFormArray;
  }

  /**
   * JAN/UPC用のサイズのフォームグループを作成して返す。(色・サイズの1行の中の1列)
   * @param size サイズ
   * @param colorCode 色コード(任意)
   * @returns サイズのフォームグループ
   */
  private createSizeGroup(size: string, colorCode?: string): FormGroup {
    const sizeFormGroup = this.formBuilder.group({
      id: '',
      colorCode: StringUtils.isNotEmpty(colorCode) ? colorCode : '',
      size: size,
      janCode: ['', {validators: Validators.required}], // Validationはフォーカスアウト時に動作
      select: false,
      brandCode: '',
      deptCode: '',
      registedJanCode: ''
    });
    this.validateSizeFormGroup(sizeFormGroup);
    return sizeFormGroup;
  }

  /**
   * JAN/UPC入力フォームの表示制御.
   * @param janType JAN区分
   */
  private showArticleNumbersForm(janType: number): void {
    // SKUがない場合は表示しない
    const inputtedArticleNumbers: SkuFormValue[] = this.articleNumbersFormArray.getRawValue();
    if (ListUtils.isEmpty(inputtedArticleNumbers)) {
      this.noShowArticleNumberNotExistSku();
      return;
    }

    // SKUが片方しかない(色、サイズ片方のみ)場合は、表示しない
    if (!this.isSkuExistBothData(inputtedArticleNumbers)) {
      this.noShowArticleNumberNotExistSku();
      return;
    }

    // SKU登録済み：
    if (janType === JanType.IN_HOUSE_JAN && this.registStatus === RegistStatus.ITEM) {
      // 自社JAN かつ 品番未登録の場合は、JAN/UPCコード入力フォーム非表示、自社JAN自動登録MSG表示
      this.isShowArticleNumbers = false;
      this.isAutomaticallyRegistInHouseJan = true;
    } else if (janType === JanType.IN_HOUSE_JAN && this.registStatus === RegistStatus.PART
      && this.isNewSizeSelected(inputtedArticleNumbers)) {
      // 自社JAN かつ 品番登録済 かつ 新たに選択したサイズがある場合は、
      // JAN / UPCコード入力フォーム表示、自社JAN自動登録MSG表示
      this.isShowArticleNumbers = true;
      this.isAutomaticallyRegistInHouseJan = true;
    } else {
      // その他の場合:他社JAN/UPC または 自社JAN(品番登録済、新たに選択したサイズなし) は、
      // JAN / UPCコード入力フォームを表示、自社JAN自動登録MSG非表示
      this.isShowArticleNumbers = true;
      this.isAutomaticallyRegistInHouseJan = false;
    }
  }

  /**
   * SKUが入力されていない場合、JAN/UPC入力フォーム非表示設定.
   * 「※色・サイズを入力してからJAN/UPCを入力してください。」のメッセージが表示される
   */
  private noShowArticleNumberNotExistSku(): void {
    this.isShowArticleNumbers = false;
    this.isAutomaticallyRegistInHouseJan = false;
  }

  /**
   * JAN/UPCフォームのSKUが両方あるかチェックする.
   * @param inputtedArticleNumbers JAN/UPCフォーム値
   * @returns true:色・サイズ両方ある、false：色・サイズ片方しかない
   */
  private isSkuExistBothData(inputtedArticleNumbers: SkuFormValue[]): boolean {
    return inputtedArticleNumbers.some(inputtedSku => {
      const isColorExist = FormUtils.isNotEmpty(inputtedSku);
      const isSizeExist = inputtedSku.sizeList.some(inputtedSize => inputtedSize.select);
      return isColorExist && isSizeExist;
    });
  }

  /**
   * JAN/UPCフォームに新たに選択したサイズがあるかチェックする.
   * @param inputtedArticleNumbers JAN/UPCフォーム値
   *  @returns true:新たに選択したサイズがある、false：新たに選択したサイズがない
   */
  private isNewSizeSelected(inputtedArticleNumbers: SkuFormValue[]): boolean {
    return inputtedArticleNumbers.some(inputtedSku =>
      inputtedSku.sizeList.some(inputtedSize => inputtedSize.select && FormUtils.isEmpty(inputtedSize.id)));
  }

  /**
   * SKUで色コードが変更された場合、JAN/UPCコードFormを変更する(色・1行).
   * @param colorInfo 新たに入力された色情報
   */
  private changeArticleNumbersRow(changeColorSku: Sku): void {
    // 指定したSKU行番号の行の色コードを変更
    const changeIndex = this.fCtrlArticleNumbers.findIndex(articleNumber =>
      articleNumber.get('rowIndex').value === changeColorSku.rowIndex);
    const rowArticleNumber = this.fCtrlArticleNumbers[changeIndex];
    // 色コード情報書き換え
    rowArticleNumber.patchValue({
      colorCode: changeColorSku.colorCode,
      colorName: changeColorSku.colorName
    });
    // JAN/UPCコードのサイズリストの中のカラーコードも全て書き換え
    const articleNumberSizeFormArray = rowArticleNumber.get('sizeList') as FormArray;
    articleNumberSizeFormArray.controls.forEach(size => size.patchValue({ colorCode: changeColorSku.colorCode }));
  }

  /**
   * SKUで色コードが追加された場合、JAN/UPCコードFormを追加する(色・1行).
   * ※追加する行の位置はSKUと同じ
   * @param inputColorSku 新たに入力されたSKU
   */
  private addArticleNumbersRow(inputColorSku: Sku): void {
    this.articleNumbersFormArray.push(this.createArticleNumbersDefaultRow(inputColorSku));
    this.fCtrlArticleNumbers.sort((a, b) => {
      if (a.get('rowIndex').value > b.get('rowIndex').value) {
        return 1;
      }
      if (a.get('rowIndex').value < b.get('rowIndex').value) {
        return -1;
      }
    });
    // JAN/UPC入力フォーム表示制御：
    this.showArticleNumbersForm(this.selectedJanType);
  }

  /**
   * JAN/UPCコードの色・1行のフォーム(組成詳細の値はデフォルト値)を作成.
   * @param colorInfo 色情報
   * @returns 組成(混率)のFormGroup
   */
  private createArticleNumbersDefaultRow(colorInfo: Sku): FormGroup {
    // 空のsizeList(FormArray)作成
    const articleNumberSizeList = this.createSizeFormArray(this.sizeMasterList);
    // SKUで選択されているサイズのリストを取得
    const skuSizeSelectedList = this.fValSkus[colorInfo.rowIndex].sizeList
      .filter(skuSize => skuSize.select);

    // 色コード、バリデーション、活性状態をセットする
    articleNumberSizeList.controls.forEach(articleNumberSize => {
      articleNumberSize.patchValue({ colorCode: colorInfo.colorCode });

      const isSizeSelected = skuSizeSelectedList.some(selectedSize => selectedSize.size === articleNumberSize.get('size').value);
      if (isSizeSelected) {
        articleNumberSize.get('select').setValue(true);
        if (this.selectedJanType === JanType.IN_HOUSE_JAN) {
          // 自社JANの場合、SKUで選択されているJAN/UPCコードの入力ボックスは非活性
          articleNumberSize.disable();
        } else {
          // 他社JAN/UPCの場合、SKUで選択されているJAN/UPCコードの入力ボックスは活性
          articleNumberSize.enable();
        }
      } else {
        // SKUで選択されていないJAN/UPCコードの入力ボックスは非活性にする
        articleNumberSize.disable();
      }
    });

    const colorForm = this.formBuilder.group({
      colorCode: colorInfo.colorCode,
      colorName: colorInfo.colorName,
      sizeList: articleNumberSizeList,
      rowIndex: colorInfo.rowIndex,
    });
    return colorForm;
  }

  /**
   * SKUで色コードが削除された場合、JAN/UPCコードFormを削除する(色・1行).
   * 指定したSKU行番号のJAN/UPCコードFormを削除する
   * @param rowIndex SKUの行番号
   */
  private deleteArticleNumbersRow(rowIndex: number): void {
    // 指定したSKU行番号の行を削除
    const deleteIndex = this.fCtrlArticleNumbers.findIndex(articleNumber => articleNumber.get('rowIndex').value === rowIndex);
    this.articleNumbersFormArray.removeAt(deleteIndex);

    // JAN/UPC入力フォーム表示制御：
    this.showArticleNumbersForm(this.selectedJanType);
  }

  /**
   * 色コードが変更された際のArticleNumber行数変更.
   */
  private changeArticleNumbersRowByColorInput(): void {
    this.changeArticleNumbersRowSubscription = this.itemDataService.changeColor$.subscribe(
      sku => {
        if (ObjectUtils.isNullOrUndefined(sku)) {
          return;
        }

        //  商品編集時、SKU未登録(JAN/UPC入力フォームなし)の場合は、JAN/UPCフォーム新規作成
        if (FormUtils.isEmpty(this.articleNumbersFormArray)) {
          this.resetArticleNumberFormArray();
        }

        // ArticleNumbersFormに指定行が存在する場合か
        const isRowExist = this.fCtrlArticleNumbers.some(articleNumber => articleNumber.get('rowIndex').value === sku.rowIndex);

        // 色コードなし、指定行が存在する場合は、行削除
        if (StringUtils.isEmpty(sku.colorCode) && isRowExist) {
          this.deleteArticleNumbersRow(sku.rowIndex);
          return;
        }
        // 色コードあり、指定行が存在する場合は、行の色コード変更
        if (StringUtils.isNotEmpty(sku.colorCode) && isRowExist) {
          this.changeArticleNumbersRow(sku);
          return;
        }
        // 色コードあり、指定行が存在しない場合は、行追加(色・1行)
        if (StringUtils.isNotEmpty(sku.colorCode) && !isRowExist) {
          this.addArticleNumbersRow(sku);
          return;
        }
      });
  }

  /**
   * SKUのチェックが変更された際のArticleNumber活性状態変更.
   */
  private changeArticleNumberDisable(): void {
    this.changeJanCodeDisableSubscription = this.itemDataService.changeSize$.subscribe(
      sku => {
        if (sku === null || StringUtils.isEmpty(sku.colorCode) || FormUtils.isEmpty(this.fCtrlArticleNumbers)) {
          // チェックしたSKUがない または 色コードがない または JAN/UPCフォームがない場合は処理しない
          return;
        }
        const disableChangeRowCtrl = this.fCtrlArticleNumbers.find(skuValue => skuValue.get('rowIndex').value === sku.rowIndex);
        const disableChangeSizeCtrl = (<FormArray> disableChangeRowCtrl.get('sizeList')).controls
          .find(jan => jan.get('size').value === sku.size);
        if (sku.select) {
          // 選択されているSKUのJAN/UPC入力ボックスは活性にする
          disableChangeSizeCtrl.get('select').setValue(true);
          if (this.selectedJanType === JanType.IN_HOUSE_JAN && this.registStatus === RegistStatus.PART) {
            // 自社JAN かつ 品番登録済みの場合、非活性
            disableChangeSizeCtrl.disable();
          } else {
            // その他の場合は活性
            // 他社JAN/UPC または 自社JAN・品番未登録
            disableChangeSizeCtrl.enable();
          }
        } else {
          // 選択されていないSKUのJAN/UPC入力ボックスは入力値を削除し、非活性にする
          disableChangeSizeCtrl.get('janCode').setValue(null);
          disableChangeSizeCtrl.get('select').setValue(false);
          disableChangeSizeCtrl.disable();
        }

        // JAN/UPC入力フォーム表示制御：
        this.showArticleNumbersForm(this.selectedJanType);
      }
    );
  }

  /**
   * SKUフォーム作成後にJAN/UPC入力フォーム作成.
   */
  private createArticleNumberFormAfterSkuFormCreated(): void {
    this.createArticleNumbersFormSubscription = this.itemDataService.isCreateSkus$.subscribe(
      isCreateSkus => {
        this.isSkuFormCreated = isCreateSkus;

        if (isCreateSkus) {
          this.createArticleNumberForm();
        }
      });
  }

  /**
   * 親画面でJAN区分が変更された場合のJAN/UPCフォーム制御.
   *
   * ・JANコード入力欄バリデーション切替
   * ・JAN/UPCフォーム表示の制御
   */
  private janTypeChangeArticleNumbers(): void {
    this.janTypeSubscription = this.itemDataService.janType$.subscribe(
      janType => {
        // JAN区分が渡されていない、またはJAN区分が変更されなかった場合は処理しない
        if (janType === null || this.preJanType === janType) {
          return;
        }

        if (!this.isSkuFormCreated) {
          return;
        }

        // 初期表示(preJanTypeがnull)時は、preJanTypeに値をセットする。
        if (this.preJanType === null) {
          this.preJanType = janType;
          return;
        }

        // JAN区分が変更された場合、Form再作成
        if (this.preJanType !== janType) {
          this.preJanType = janType;
          // Form再作成
          // ※JAN区分変更時はJANコード値を保持しないため、JANコード値をクリアする
          this.clearJanCode();
          this.createArticleNumberForm();

          // JAN/UPCフォーム表示制御
          this.showArticleNumbersForm(janType);
        }
      });
  }

  /**
   * 入力されているJANコード値をクリアする
   */
  private clearJanCode() {
    this.fCtrlSkus.forEach(skuCtrl => {
      (<FormArray> skuCtrl.get('sizeList')).controls.filter(sizeCrtl =>
        FormUtils.isNotEmpty((<FormGroup> sizeCrtl).controls.janCode))
        .forEach(existJanCodeSizeCrtl => (<FormGroup> existJanCodeSizeCrtl).controls.janCode.setValue(null));
    });
  }

  /**
   * JAN/UPCコードのサイズのフォームグループのバリデーションをセット
   * @param sizeFormGroup サイズのフォームグループ
   */
  private validateSizeFormGroup(sizeFormGroup: FormGroup): void {
    switch (this.selectedJanType) {
      case JanType.IN_HOUSE_JAN:
        // 自社JANの場合はバリデーションセットしない
        sizeFormGroup.controls.janCode.setValidators(null);
        break;
      case JanType.OTHER_JAN:
        sizeFormGroup.controls.janCode.setValidators([
          Validators.required,
          Validators.pattern(ValidatorsPattern.NUMERIC_0_9_OTHER_JAN)
        ]);
        break;
      case JanType.OTHER_UPC:
        sizeFormGroup.controls.janCode.setValidators([
          Validators.required,
          Validators.pattern(ValidatorsPattern.NUMERIC_0_9_OTHER_UPC)
        ]);
        break;
      default:
        break;
    }
  }

  /**
   * JAN/UPC入力欄のバリデーションエラーがあるかの判定処理.
   *
   * 以下バリデーションエラーがある場合はエラーメッセージ表示：
   * ・必須エラー
   * ・パターン(文字種、文字数)エラー
   * ・APIチェックエラー ※1件でも必須エラー(未入力あり)またはパターンエラーがある場合、表示されない
   *
   * @return エラーあり：true、エラーなし：false
   */
  isArticleNumberValidationError(): boolean {
    this.articleNumberErrorMsg = []; // JAN/UPCエラーメッセージクリア
    let isValidatorError = false;

    // JAN/UPCフォームが存在しない場合は処理しない
    if (FormUtils.isEmpty(this.fCtrlArticleNumbers)) {
      return isValidatorError;
    }

    // エラー存在フラグ
    let existsRequiredError = false; // 必須エラーが1件でも存在するか
    let existsPatternError = false; // パターン(文字種、文字数)エラーが1件でも存在するか
    let existCheckApiError = false; // JAN/UPCコードのAPIチェックエラーが1件でも存在するか

    let checkApiErrorList: {code: string , arg: string}[] = []; // JAN/UPC入力欄のチェックAPIエラーのリスト

    // required(必須)エラー
    existsRequiredError = this.isRequiredErrorExist();
    if (existsRequiredError) {
      this.articleNumberErrorMsg.push({ code: 'ERRORS.VALIDATE.ARTICLE_NUMBER_EMPTY', arg: null });
    }

    // pattern(文字種、文字数)エラー
    existsPatternError = this.isPatternErrorExist();
    if (existsPatternError) {
      if (this.selectedJanType === JanType.OTHER_JAN) {
        this.articleNumberErrorMsg.push({ code: 'ERRORS.VALIDATE.PATTERN_OTHER_JAN', arg: null });
      } else if (this.selectedJanType === JanType.OTHER_UPC) {
        this.articleNumberErrorMsg.push({ code: 'ERRORS.VALIDATE.PATTERN_OTHER_UPC', arg: null });
      }
    }

    // APIチェックエラー
    checkApiErrorList = this.checkApiErrorList();
    if (ListUtils.isNotEmpty(checkApiErrorList)) {
      existCheckApiError = true;
      checkApiErrorList.forEach(checkApiError => {
        const janError: JanError = { code: 'ERRORS.' + checkApiError.code, arg: checkApiError.arg };
        // 同じエラーメッセージが存在しない場合のみpush
        const isErrorExist = this.articleNumberErrorMsg.some(error => error.code === janError.code && error.arg === janError.arg);
        if (!isErrorExist) {
          this.articleNumberErrorMsg.push(janError);
        }
      });
    }

    // エラーが1件でもあれば、エラーメッセージ表示
    if (existsRequiredError || existsPatternError || existCheckApiError) {
      isValidatorError = true;
    }

    return isValidatorError;
  }

  /**
   * JAN/UPC入力欄の必須エラーがあるか.
   *
   * @return 必須エラーあり：true、必須エラーなし：false
   */
  private isRequiredErrorExist(): boolean {
    let isExistsRequiredError = false;

    this.fCtrlArticleNumbers.some(articleNumberCtrl => {
      const sizeListCtrl = (<FormArray> articleNumberCtrl.get('sizeList')).controls;
      // 必須エラーが1件でも存在すればtrue
      isExistsRequiredError = sizeListCtrl.some(sizeCtrl => {
        const janCode = <FormControl> sizeCtrl.get('janCode');
        return FormUtils.isErrorDisplayWithSubmit(janCode, this.submitted) && janCode.errors.required;
      });
      return isExistsRequiredError;
    });

    return isExistsRequiredError;
  }

  /**
   * JAN/UPC入力欄のパターン(文字種、文字数)エラーがあるか.
   *
   * @return パターンエラーあり：true、パターンエラーなし：false
   */
  private isPatternErrorExist(): boolean {
    let isExistsPatternError = false;

    this.fCtrlArticleNumbers.some(articleNumberCtrl => {
      const sizeListCtrl = (<FormArray> articleNumberCtrl.get('sizeList')).controls;
      // パターンエラーが1件でも存在すればtrue
      isExistsPatternError = sizeListCtrl.some(sizeCtrl => {
        const janCode = <FormControl> sizeCtrl.get('janCode');
        return FormUtils.isErrorDisplayWithSubmit(janCode, this.submitted) && janCode.errors.pattern;
      });
      return isExistsPatternError;
    });

    return isExistsPatternError;
  }

  /**
   * JAN/UPC入力欄のチェックAPIエラーリスト取得.
   *
   * @return JAN/UPC入力欄のチェックAPIエラーのリスト
   */
  private checkApiErrorList(): JanError[] {
    // JAN/UPC入力欄のチェックAPIエラーのリスト
    const checkApiErrorList: JanError[] = [];

    // エラーがあればエラーメッセージリストにセット
    if (FormUtils.isNotEmpty(this.articleNumbersFormArray.errors)) {
      const errorDetails = this.articleNumbersFormArray.errors['validate'] as ErrorDetail[];

      if (FormUtils.isNotEmpty(errorDetails)) {
        errorDetails.forEach(errorDetail => {
          checkApiErrorList.push({ code: errorDetail.code, arg: errorDetail.args[0] } as JanError);
        });
      }
    }

    return checkApiErrorList;
  }

  /**
   * JAN/UPC入力欄がエラーか.
   *
   * @param sizeCtrl AbstractControl
   * @return エラー：true、エラーなし：false
   */
  isJanCodeError(sizeCtrl: AbstractControl): boolean {
    const janCode = sizeCtrl.get('janCode').value as string;
    let isApiErrorJanCode = false; // JAN/UPCコードはAPIエラーに含まれているか

    // APIエラーに含まれるJAN/UPCコードは、入力欄はエラーになる
    if (FormUtils.isNotEmpty(this.articleNumbersFormArray.errors)) {
      const errorDetails = this.articleNumbersFormArray.errors['validate'] as ErrorDetail[];

      if (FormUtils.isNotEmpty(errorDetails)) {
        isApiErrorJanCode = errorDetails.some((errorDetail) => errorDetail.args[0] === janCode);
      }
    }

    return FormUtils.isErrorDisplayWithSubmit(sizeCtrl, this.submitted) || isApiErrorJanCode;
  }
}
