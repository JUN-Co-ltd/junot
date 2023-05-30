import { Component, OnInit, Input, Output, OnChanges, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, FormControl, Validators, AbstractControl } from '@angular/forms';
import { EventEmitter } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateService } from '@ngx-translate/core';

import { Const, RegistStatus, SearchTextType, JanType, ViewMode } from '../../../const/const';
import { ListUtils } from '../../../util/list-utils';
import { StringUtils } from '../../../util/string-utils';
import { FormUtils } from 'src/app/util/form-utils';
import { ObjectUtils } from 'src/app/util/object-utils';
import { ExceptionUtils } from 'src/app/util/exception-utils';

import { SearchColorModalComponent } from '../../search-color-modal/search-color-modal.component';

import { JunpcCodmstService } from '../../../service/junpc-codmst.service';
import { LoadingService } from '../../../service/loading.service';
import { ItemDataService } from '../../../service/shared/item-data.service';

import { JunpcCodmstSearchCondition } from '../../../model/junpc-codmst-search-condition';
import { Sku } from '../../../model/sku';
import { JunpcCodmst } from '../../../model/junpc-codmst';
import { JunpcSizmst } from '../../../model/junpc-sizmst';

import { tap, finalize } from 'rxjs/operators';
import { SkuFormValue } from 'src/app/interface/sku-form-value';

@Component({
  selector: 'app-sku-select',
  templateUrl: './sku-select.component.html',
  styleUrls: ['./sku-select.component.scss']
})
export class SkuSelectComponent implements OnInit, OnChanges {
  private addErrorMessage = ''; // SKUForm追加最大件数超えエラーメッセージ

  @Input() parentForm: FormGroup = null;              // 親画面のForm
  @Input() private skusValue: Sku[] = null;           // 処理中の品番のsku(色・コード)情報リスト
  @Input() private registStatus = RegistStatus.ITEM;  // 親画面で編集中のデータの登録ステータス
  @Input() private partNoKind = '';                   // 親画面で入力した品種
  @Input() sizeMasterList: JunpcSizmst[] = [];        // 親画面で取得したサイズマスタのデータリスト
  @Input() submitted = false;                         // 親画面でsubmitしたか
  @Input() viewMode = ViewMode.ITEM_NEW;              // 親画面の画面表示モード
  @Input() private isCopy = false;                    // コピー新規作成中か
  isNoSizeMasta = true; // サイズマスタ未取得フラグ(SKU入力フォーム表示フラグ)

  @Output() private changeColor = new EventEmitter(); // 正常な色コードを入力した時のEventEmitter
  @Output() private deleteColor = new EventEmitter(); // 色コードの入力を削除した時のEventEmitter

  constructor(
    private formBuilder: FormBuilder,
    private translate: TranslateService,
    private junpcCodmstService: JunpcCodmstService,
    private loadingService: LoadingService,
    private itemDataService: ItemDataService,
    private modalService: NgbModal
  ) {}

  /**
   * 親より渡されたJAN区分(画面上で選択したJAN区分).
   * @return this.itemDataService.janType$.value
   */
  get selectedJanType() {
    return this.itemDataService.janType$.value;
  }

  /**
   * parentFormのskuFormArrayを取得する.
   * @return this.parentForm.get('skus')
   */
  get skusFormArray(): FormArray {
    return this.parentForm.get('skus') as FormArray;
  }

  /**
   * parentFormのskuFormArrayの項目の状態を取得する。
   * @return this.mainForm.get('skus').controls
   */
  get fCtrlSkus(): AbstractControl[] {
    return this.skusFormArray.controls;
  }

  /**
   * parentFormのskuFormArrayのvalueを返す.
   * @return this.parentForm.get('skus').value
   */
  get fValSkus(): SkuFormValue[] {
    return this.skusFormArray.value;
  }

  ngOnInit() {
    this.translate.get('ERRORS.VALIDATE.NO_MORE_ADD').subscribe((value: string) => this.addErrorMessage = value);
  }

  /**
   * データバインドされた入力プロパティが変更される度に呼び出される。
   * @param changes 変更されたプロパティ
   */
  ngOnChanges(changes: SimpleChanges) {
    if (changes.partNoKind) { // 品種が変更した場合
      // サイズマスタが取得できなかった または 品番が空の場合は、
      // SKUフォーム非表示
      // JAN/UPCフォームもクリアし、非表示にする
      if (ListUtils.isEmpty(this.sizeMasterList) || FormUtils.isEmpty(this.partNoKind)) {
        this.invisibleSkuAndArticleNumbers();
        return;
      }

      // 品種入力値変更時の処理
      this.createForm(this.skusValue);
    }
  }

  /**
   * parentFormのskuFormをクリアする.
   */
  private clearSkuFormArray(): void {
    this.parentForm.setControl('skus', this.formBuilder.array([]));
  }

  /**
   * SkuForm追加ボタン押下時の処理
   * SKUFormを追加する。
   */
  onAddSkuForm(): void {
    // 最大追加件数超えチェック
    if (this.fCtrlSkus.length >= Const.SKU_FORM_MAX_COUNT) {
      alert(this.addErrorMessage);
      return;
    }

    for (let i = 0; i < Const.SKU_FORM_ADD_COUNT; i++) {
      const currentLength = this.fCtrlSkus.length;
      if (currentLength >= Const.SKU_FORM_MAX_COUNT) { break; } // 50件まで
      this.skusFormArray.push(this.createSkuFormGroupForm(this.sizeMasterList));
    }
  }

  /**
   * 親から渡されたサイズマスタを基にskuFormを作成する。
   * 親画面のformで品種入力時、または初期表示時に当関数が実行される。
   * skuFormに色コード入力済みである場合は、チェック状態を新しいskuFormに設定して作成する。
   * ※新たに入力された品種のサイズマスタにないサイズのチェックは外す。
   * @param skusValue 呼び元から渡されたsku(色・コード)の情報
   */
  private createForm(skusValue: Sku[]): void {
    let loadingToken = null;

    // ローディング開始
    this.loadingService.start().pipe(
      // ローディングトークン待避
      tap((token) => loadingToken = token),
      tap(() => this.isNoSizeMasta = true), // SKUフォーム非表示
      tap(() => {
        // Service経由でSKUフォームの未作成を通知する(JAN/UPCフォーム作成不可)
        this.itemDataService.isCreateSkus$.next(false);

        const inputtedSkusValues = this.getInputtedSkus();  // 既存のskuFormから色コードが入力されているskuリストを取得する。
        this.clearSkuFormArray(); // 既存のskuFormクリア(クリアしないと既存のskuFormに新たに追加される)

        // 既存のskuFormに色コード入力済みである場合、入力値をskuFormに設定して作成。
        // (ブランド変更前のSKU入力状態をブランド変更後も引き継ぐ)
        if (inputtedSkusValues.length !== 0) {
          this.createSkuFormArraySettedValue(inputtedSkusValues, this.sizeMasterList);
          return;
        }

        // 既存のskuFormに色コード未入力である場合、
        // 呼び元からsku(色・コード)の情報が渡されていない場合はskuFormを新規作成。
        // (商品新規登録時)
        if (skusValue == null || skusValue.length === 0) {
          this.createSkuFormArray(this.sizeMasterList);
          return;
        }

        // 呼び元からsku(色・コード)の情報が渡されている場合はskuFormに設定して作成。
        // (商品/品番編集時)
        this.createSkuFormArraySettedValue(skusValue, this.sizeMasterList);
      }),
      finalize(() => {
        // SKUフォームの作成完了を通知する
        this.publishCreateSkus();
        this.isNoSizeMasta = false; // SKUフォーム表示
        this.loadingService.stop(loadingToken);
      })
    ).subscribe();
  }

  /**
   * 既存のskuFormから色コードが入力されているskuリストを取得する。
   * (品種を変更しても前の入力値を引き継ぐ為)
   * @returns skuリスト
   */
  private getInputtedSkus(): Sku[] {
    const inputtedSkus: Sku[] = [];
    const skuValues: SkuFormValue[] = this.skusFormArray.getRawValue();
    skuValues.forEach(skuValue => {
      if (skuValue.colorCode !== '') {
        // 色コードが入力されている場合戻り値のリストに格納
        const sizeList = skuValue.sizeList;
        let isExistSelect = false;
        sizeList.forEach(size => {  // 全サイズ確認
          if (size.select) {
            // チェックありのサイズのsku情報を戻り値のリストに格納
            isExistSelect = true;
            const sku = new Sku();
            sku.id = size.id; // ※DBから取得したsku情報でなければ空白
            sku.colorCode = skuValue.colorCode;
            sku.colorName = skuValue.colorName;
            sku.size = size.size;
            sku.janCode = '';
            inputtedSkus.push(sku);
          }
        });
        if (!isExistSelect) {
          // チェックありが1つもなければ色コード・色名だけ設定したskuを戻り値のリストに格納
          const sku = new Sku();
          sku.colorCode = skuValue.colorCode;
          sku.colorName = skuValue.colorName;
          sku.janCode = '';
          inputtedSkus.push(sku);
        }
      }
    });
    return inputtedSkus;
  }

  /**
   * sku(色・コード)情報の値を設定したskuのフォーム配列を作成する。(色・サイズのForm全行全列)
   * @param allSkuValues formに設定するsku(色・コード)情報リスト
   * @param sizeMasterList サイズマスタ情報リスト
   */
  private createSkuFormArraySettedValue(allSkuValues: Sku[], sizeMasterList: JunpcSizmst[]): void {
    // sku(色・コード)リストから色コードの重複除去
    const uniqueSkusValue = allSkuValues.filter((sku1, idx, array) =>
      (array.findIndex(sku2 => sku2.colorCode === sku1.colorCode) === idx));

    // 色コードごとにsku(色・コード)form作成
    uniqueSkusValue.forEach(skuValue => {
      // sku(色・コード)情報リストから処理中の色コードに該当するデータを抽出する。
      const sameCodeSkuValues = allSkuValues.filter(sku => skuValue.colorCode === sku.colorCode);
      // skuFormGroup作成(サイズのFormArrayに色コード、色名、エラーフラグ情報を加えたform)
      const skuFormGroup = this.createSkuFormGroupForm(sizeMasterList, skuValue);
      // サイズのFormArray作成(サイズのチェックボックス1行全列)
      const sizeFormArray = this.createSizeFormArraySettedValue(sameCodeSkuValues, sizeMasterList);
      skuFormGroup.setControl('sizeList', sizeFormArray);
      this.skusFormArray.push(skuFormGroup);
    });

    // デフォルト行数まで不足分のForm行を追加する
    const skuFormArraylength = this.skusFormArray.length;
    for (let i = 0; i < Const.SKU_FORM_DEFAULT_COUNT - skuFormArraylength; i++) {
      this.skusFormArray.push(this.createSkuFormGroupForm(sizeMasterList));
    }
  }

  /**
   * sku(色・コード)情報の値を設定したサイズのform配列を作成して返す。(色・サイズの1行の中の全列)
   * 引数のskuリストに存在するサイズでもサイズマスタに存在しなければ作成しない。
   * @param sameCodeSkuValues formに設定するsku(色・コード)情報リスト※リスト内のデータの色コードは全て同じ
   * @param sizeMasterList サイズマスタ情報リスト
   * @returns サイズのフォーム配列
   */
  private createSizeFormArraySettedValue(sameCodeSkuValues: Sku[], sizeMasterList: JunpcSizmst[]): FormArray {
    const sizeFormArray = this.formBuilder.array([]);

    // 取得したサイズマスタ分チェックボックスform作成(1行の中の全列作成)
    sizeMasterList.forEach(sizeMaster => {
      // 1列デフォルトformGroup作成
      const oneSizeFormGroup = this.createSizeGroup(sizeMaster.szkg, sameCodeSkuValues[0].colorCode);
      // sku(色・コード)データ設定
      sameCodeSkuValues.forEach(skuValue => {
        // 処理中のsku(色・コード)情報のサイズは処理中の取得済みのサイズマスタに存在するか
        if (sizeMaster.szkg === skuValue.size) {
          // 存在する場合はskuのID、チェックボックス設定
          oneSizeFormGroup.patchValue({ id: this.isCopy ? null : skuValue.id });  // ID設定。コピー新規の場合は新規採番する為null
          oneSizeFormGroup.patchValue({ janCode: this.isCopy ? null : skuValue.janCode });  // JANコード設定。コピー新規の場合は新規採番する為null
          oneSizeFormGroup.setControl('select', new FormControl(true));  // チェックボックスon
        }
      });
      sizeFormArray.push(oneSizeFormGroup);
    });
    return sizeFormArray;
  }

  /**
   * sku(色・サイズ)のフォーム配列を作成する。(色・サイズのForm全行全列).
   * @param sizeMasterList サイズマスタ情報リスト
   */
  private createSkuFormArray(sizeMasterList: JunpcSizmst[]): void {
    for (let i = 0; i < Const.SKU_FORM_DEFAULT_COUNT; i++) {
      this.skusFormArray.push(this.createSkuFormGroupForm(sizeMasterList));
    }
  }

  /**
   * sku(色・サイズ)のフォームグループを作成して返す。(色・サイズの1行).
   * @param sizeMasterList サイズマスタ情報リスト
   * @param skuValue sku(色コード)情報
   */
  private createSkuFormGroupForm(sizeMasterList: JunpcSizmst[], skuValue?: Sku): FormGroup {
    let colorCode = '';
    let colorName = '';
    if (!ObjectUtils.isNullOrUndefined(skuValue)) {
      colorCode = skuValue.colorCode;
      colorName = skuValue.colorName;
    }

    const skuFormGroup = this.formBuilder.group({
      colorCode: [colorCode, [
        Validators.maxLength(2),
        Validators.pattern(/^([0-9][1-9]|[1-9][0-9])$/)
      ]],
      colorName: colorName,
      sizeList: this.createSizeFormArray(sizeMasterList),
      rowIndex: ''
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
   * SKU用のサイズのフォームグループを作成して返す。(色・サイズの1行の中の1列)
   * @param size サイズ
   * @param colorCode 色コード(任意)
   * @returns サイズのフォームグループ
   */
  private createSizeGroup(size: string, colorCode?: string): FormGroup {
    return this.formBuilder.group({
      id: '',
      colorCode: StringUtils.isNotEmpty(colorCode) ? colorCode : '',
      size: size,
      janCode: '',
      select: false,
      brandCode: '',
      deptCode: '',
      registedJanCode: ''
    });
  }

  /**
   * SKU色コードの非活性制御.
   * 以下の場合は色コード入力ボックス・色コード検索ボタン非活性：
   *   未登録SKU かつ 品番登録済み かつ 商品情報画面 かつ 自社JAN以外
   *   または
   *   登録済SKU かつ 品番登録済み
   *
   * @param colorItem sku(色1行)のフォームグループ
   */
  isColorCodeDisabled(colorItem: AbstractControl): boolean {
    let isColorDisable = false;
    const sizeListCtrl = (<FormArray> colorItem.get('sizeList')).controls;

    sizeListCtrl.some(sizeCtrl => {
      if (this.isSkuDisabled(sizeCtrl)) {
        isColorDisable = true;
        return true;
      }
    });
    return isColorDisable;
  }

  /**
   * SKUチェックボックスの非活性制御.
   * 以下の場合はチェックボックス非活性：
   *   未登録SKU かつ 品番登録済み かつ 商品情報画面 かつ 自社JAN以外
   *   または
   *   登録済SKU かつ 品番登録済み
   *
   * @param sku sku(サイズ1列)のフォームグループ
   */
  isSkuDisabled(sku: AbstractControl): boolean {
    // IDあり：登録済SKU、IDなし：未登録SKU
    const isSkuRegistered: boolean = FormUtils.isNotEmpty(sku.get('id').value);

    return (!isSkuRegistered && this.registStatus === RegistStatus.PART
      && this.viewMode === ViewMode.ITEM_EDIT && this.selectedJanType !== JanType.IN_HOUSE_JAN)
      || (isSkuRegistered && this.registStatus === RegistStatus.PART);
  }

  /**
   * 色検索を行うモーダルを表示する。
   * @param rowIndex 検索ボタン押下した色のskusFormArrayの行番号
   */
  openSearchColorModal(rowIndex: number): void {
    const modalRef = this.modalService.open(SearchColorModalComponent);

    // 色コードを取得済みであれば検索モーダルの検索条件に設定
    const inputtedColorCode = this.fValSkus[rowIndex].colorCode;
    modalRef.componentInstance.searchCondition = {
      searchType: SearchTextType.CODE_NAME_PARTIAL_MATCH,
      searchText: inputtedColorCode
    } as JunpcCodmstSearchCondition;

    // 色コードを取得済みであれば検索モーダルの初期値に設定
    modalRef.componentInstance.default = { code1: inputtedColorCode } as JunpcCodmst;

    // モーダルで取得したデータ設定
    modalRef.result.then((result: JunpcCodmst) => {
      if (result) {
        const colorCode = result.code1;
        const colorName = result.item2;
        this.fCtrlSkus[rowIndex].get('colorCode').setValue(colorCode);
        this.fCtrlSkus[rowIndex].get('colorName').setValue(colorName);
        // 親画面の混率入力form設定
        const inputColorSku = {
          colorCode: colorCode,
          colorName: colorName
        } as Sku;
        this.changeColor.emit(inputColorSku);  // 親画面の混率入力formを表示
        this.publishChangeColor(rowIndex, inputColorSku);  // 色コードの変更を通知する
      }
    }, () => { });  // バツボタンクリック時は何もしない
  }

  /**
   * 色コードの入力値を変更したときの処理
   * @param colorCode 色コード
   * @param rowIndex 変更した色コードのskusFormArrayの行番号
   */
  onChangeColorCode(colorCode: string, rowIndex: number): void {
    const colorLen = colorCode.length;
    if (colorLen !== 0 && colorLen !== 2) { return; } // 0桁でも2桁でもない場合は何もしない

    this.fCtrlSkus[rowIndex].get('colorName').setValue('');
    this.fCtrlSkus[rowIndex].setErrors(null);
    if (colorLen === 0) {
      this.deleteColor.emit();  // 親画面の混率入力formを削除
      this.publishChangeColor(rowIndex); // 色コードの変更を通知する
    }
    if (colorLen === 2) {
      this.getColorNameByColorCode(colorCode, rowIndex);
    }
  }

  /**
   * 色コードをキーにコードマスタから色名を取得してformに設定する。
   * @param colorCode 色コード
   * @param rowIndex 入力した色コードのskusFormArrayの行番号
   */
  private getColorNameByColorCode(colorCode: string, rowIndex: number): void {
    this.junpcCodmstService.getColors({
      searchType: SearchTextType.CODE_PARTIAL_MATCH,
      searchText: colorCode
    } as JunpcCodmstSearchCondition).subscribe(
      responseData => {
        if (responseData == null || ListUtils.isEmpty(responseData.items)) {
          this.handleGetColorsApiError(rowIndex);
          return;
        }
        const colorName = responseData.items[0].item2;
        this.fCtrlSkus[rowIndex].get('colorName').setValue(colorName);
        const inputColorSku = {
          colorCode: colorCode,
          colorName: colorName
        } as Sku;
        this.changeColor.emit(inputColorSku); // 親画面の混率入力formを表示
        this.publishChangeColor(rowIndex, inputColorSku); // 色コードの変更を通知する
      }, (error: HttpErrorResponse) => this.handleGetColorsApiError(rowIndex, error)
    );
  }

  /**
   * 色取得エラー時の処理.
   * @param rowIndex 入力した色コードのskusFormArrayの行番号
   * @param error エラー情報
   */
  private handleGetColorsApiError(rowIndex: number, error: HttpErrorResponse = null): void {
    let errorCode = 'ERRORS.400_01';
    const apiError = ExceptionUtils.apiErrorHandler(error);
    if (apiError != null) {
      errorCode = apiError.viewErrorMessageCode;
    }

    this.translate.get(errorCode).subscribe((text: string) => {
      // 色名にエラーメッセージを表示
      this.fCtrlSkus[rowIndex].get('colorName').setValue(text);
      this.fCtrlSkus[rowIndex].setErrors({ 'existsData': true });
      this.deleteColor.emit();  // 親画面の混率入力formを削除
      this.publishChangeColor(rowIndex); // 色コードの変更を通知する
    });
  }

  /**
   * サイズのチェックを変更したときの処理
   * @param colorCode 色コード
   * @param rowIndex 変更したサイズの行番号
   * @param colIndex 変更したサイズのsizeFormArrayの列番号
   */
  onChangeSizeCheck(colorCode: string, size: AbstractControl, rowIndex: number): void {
    if (FormUtils.isEmpty(colorCode)) { return; } // 色コードが入力されていない場合は処理しない
    const checkChangeSku = {
      colorCode: colorCode,
      size: size.get('size').value,
      rowIndex: rowIndex,
      select: size.get('select').value
    } as Sku;
    // Service経由でSKUサイズの変更感知
    this.itemDataService.changeSize$.next(checkChangeSku);
  }

  /**
   * 色コードの変更を通知する.
   *
   * ・inputColorSkuあり：JAN/UPC行追加・変更
   * ・inputColorSkuなし：JAN/UPC行削除
   * @param rowIndex SKUの行番号
   * @param inputColorSku 入力したSKU
   */
  private publishChangeColor(rowIndex: number, inputColorSku?: Sku): void {
    let sku = new Sku();
    if (FormUtils.isNotEmpty(inputColorSku)) {
      sku = inputColorSku;
    } else {
      sku.colorCode = null;
    }
    // SKUの行番号をセット
    sku.rowIndex = rowIndex;
    // Service経由でSKU色コードの変更感知
    this.itemDataService.changeColor$.next(sku);
  }

  /**
   * SKUフォームの作成を通知する.
   */
  private publishCreateSkus(): void {
    if (ListUtils.isNotEmpty(this.fCtrlSkus)) {
      // SKUフォームの行番号をセット
      this.fCtrlSkus.forEach(skuCtrl => skuCtrl.get('rowIndex').setValue(this.fCtrlSkus.indexOf(skuCtrl)));
    }

    // Service経由でSKUフォームの作成完了を通知する(JAN/UPCフォーム作成可能)
    this.itemDataService.isCreateSkus$.next(true);
  }

  /**
   * SKUとJAN/UPC入力フォームを非表示にする.
   * ・品種が削除された場合
   * ・サイズマスタが取得できなかった場合
   */
  private invisibleSkuAndArticleNumbers(): void {
    this.isNoSizeMasta = true;
    // SKUフォームの作成を通知する(JAN/UPCフォームもクリアし、非表示になる)
    this.publishCreateSkus();
  }
}
