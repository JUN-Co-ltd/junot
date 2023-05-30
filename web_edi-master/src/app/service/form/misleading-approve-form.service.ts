import { Injectable } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, AbstractControl } from '@angular/forms';

import { from } from 'rxjs';
import { tap, distinct } from 'rxjs/operators';

import { MisleadingRepresentationInspectionForm } from './interface';

import { MisleadingRepresentationType } from 'src/app/const/const';

import { StringUtils } from 'src/app/util/string-utils';
import { ObjectUtils } from 'src/app/util/object-utils';
import { ListUtils } from 'src/app/util/list-utils';
import { DateUtils } from 'src/app/util/date-utils';

import { Sku } from 'src/app/model/sku';
import { ItemMisleadingRepresentation } from 'src/app/model/item-misleading-representation';
import { MisleadingRepresentation } from 'src/app/model/misleading-representation';

/**
 * 優良誤認承認画面のフォームサービス
 */
@Injectable()
export class MisleadingApproveFormService {

  private _form: FormGroup;
  private _approvalUserAccountName: string;  // ログインユーザーアカウント
  private _approvalUserName: string; // ログインユーザー名称

  /**
   * @returns 組成検査FormArray
   */
  get compositionInspectionsArray(): FormArray {
    return this._form.get('compositionInspections') as FormArray;
  }

  /**
   * @param approvalUserAccountName ログインユーザーアカウント
   */
  set approvalUserAccountName(approvalUserAccountName: string) {
    this._approvalUserAccountName = approvalUserAccountName;
  }

  /**
   * @param accountName ログインユーザー名称
   */
  set approvalUserName(approvalUserName: string) {
    this._approvalUserName = approvalUserName;
  }

  constructor(
    private fb: FormBuilder
  ) {
  }

  /**
   * フォームを作成する.
   */
  private createForm(): void {
    this._form = this.fb.group({
      partNoId: null, // 品番ID
      checkAllColor: false, // 全カラーチェック
      cooInspectionGp: this.fb.group({  // 原産国検査
        id: null, // 優良誤認承認情報ID
        misleadingRepresentationType: MisleadingRepresentationType.COO, // 優良誤認検査対象区分
        cooCode: null,  // 原産国コード
        check: null,  // チェック
        approvalUserAccountName: null,  // 承認者アカウント名
        approvalUserName: null, // 承認者アカウント名称
        approvalAt: null, // 承認日
        memo: null,  // メモ
        updatedAt: null  // 更新日時
      }),
      compositionInspections: this.fb.array([]),  // 組成検査
      harmfulInspectionGp: this.fb.group({  // 染料有害検査
        id: null, // 優良誤認承認情報ID
        misleadingRepresentationType: MisleadingRepresentationType.HARMFUL, // 優良誤認検査対象区分
        mdfMakerCode: null, // 生産メーカーコード
        check: null,  // チェック
        approvalUserAccountName: null,  // 承認者アカウント名
        approvalUserName: null, // 承認者アカウント名称
        approvalAt: null,  // 承認日
        memo: null,  // メモ
        updatedAt: null  // 更新日時
      })
    });
  }

  /**
   * データ設定されたフォームを作成する.
   * @param data 優良誤認情報データ
   * @returns データ設定したフォーム
   */
  generateDataSettedForm = (data: ItemMisleadingRepresentation): FormGroup => {
    this.createForm();  // 初期化
    // 組成検査FormArray作成(DB登録済であれば登録値を設定する)
    const generateValue = this.generateCompositionFormSetValue(data);
    from(data.skus).pipe(
      distinct(sku => sku.colorCode), // カラーコードごとに作成
      tap(sku => this.addCompositionInspection(generateValue(sku)))
    ).subscribe();

    const isAllApproval = this.filterItemMisleadingRepresentationByComposition(data)
      .every(comp => StringUtils.isNotEmpty(comp.approvalUserAccountName));
    this._form.patchValue({checkAllColor: isAllApproval});

    this.patchRegisteredValue(data);
    return this._form;
  }

  /**
   * 品番ID登録済の場合、登録値をフォームにpatch.
   * @param data 優良誤認情報データ
   */
  private patchRegisteredValue = (data: ItemMisleadingRepresentation): void => {
    if (ObjectUtils.isNullOrUndefined(data.id)) { return; }

    // 品番ID
    this._form.patchValue({partNoId: data.id});

    const findTargetFn = this.findTargetMisleadingRepresentationType(data);

    // 原産国
    const cooRecord = findTargetFn(MisleadingRepresentationType.COO);
    const cooRegisted = this.generateFormSetValue(cooRecord);
    this._form.patchValue({
      cooInspectionGp: {
        id: cooRegisted.id,
        misleadingRepresentationType: MisleadingRepresentationType.COO,
        cooCode: data.cooCode,
        check: cooRegisted.check,
        approvalUserAccountName: cooRegisted.approvalUserAccountName,
        approvalUserName: cooRegisted.approvalUserName,
        approvalAt: cooRegisted.approvalAt,
        memo: cooRegisted.memo,
        updatedAt: cooRegisted.updatedAt
      }
    });
    // 有害物質
    const harmfulRecord = findTargetFn(MisleadingRepresentationType.HARMFUL);
    const harmfulRegisted = this.generateFormSetValue(harmfulRecord);
    this._form.patchValue({
      harmfulInspectionGp: {
        id: harmfulRegisted.id,
        misleadingRepresentationType: MisleadingRepresentationType.HARMFUL,
        mdfMakerCode: data.mdfMakerCode,
        check: harmfulRegisted.check,
        approvalUserAccountName: harmfulRegisted.approvalUserAccountName,
        approvalUserName: harmfulRegisted.approvalUserName,
        approvalAt: harmfulRegisted.approvalAt,
        memo: harmfulRegisted.memo,
        updatedAt: harmfulRegisted.updatedAt
      }
    });
  }

  /**
   * フォームへの設定値を取得する.
   * @param registeredRecord DB登録済のレコード
   * @returns form設定値
   */
  private generateFormSetValue = (registeredRecord: MisleadingRepresentation): MisleadingRepresentationInspectionForm => {
    if (ObjectUtils.isNullOrUndefined(registeredRecord)) {
      // DB未録済の場合の設定値
      return {
        id: null,
        check: false,
        approvalUserAccountName: null,
        approvalUserName: null,
        approvalAt: null,
        memo: null,
        updatedAt: null
      } as MisleadingRepresentationInspectionForm;
    }

    if (StringUtils.isEmpty(registeredRecord.approvalUserAccountName)) {
      // 承認者がnullの場合の設定値
      return {
        id: registeredRecord.id,
        check: false,
        approvalUserAccountName: null,
        approvalUserName: null,
        approvalAt: null,
        memo: registeredRecord.memo,
        updatedAt: registeredRecord.updatedAt
      } as MisleadingRepresentationInspectionForm;
    }

    // 承認者がnullではない場合の設定値
    return {
      id: registeredRecord.id,
      check: true,
      approvalUserAccountName: registeredRecord.approvalUserAccountName,
      approvalUserName: registeredRecord.approvalUserName,
      approvalAt: registeredRecord.approvalAt,
      memo: registeredRecord.memo,
      updatedAt: registeredRecord.updatedAt
    } as MisleadingRepresentationInspectionForm;
  }

  /**
   * 組成検査フォームの設定値を作成する.
   * @param data 優良誤認情報データ
   * @param sku 処理中のsku
   * @returns 組成検査フォームの設定値
   */
  private generateCompositionFormSetValue = (data: ItemMisleadingRepresentation) => (sku: Sku)
    : MisleadingRepresentationInspectionForm => {
    // 組成の優良誤認承認データのみ抽出
    const compositions = ListUtils.isEmpty(data.misleadingRepresentations) ? null :
      data.misleadingRepresentations.filter(mr => MisleadingRepresentationType.COMPOSITION === mr.misleadingRepresentationType);
    // 登録済の組成の優良誤認承認データから処理中のカラーコードを抽出
    const compositionRecord = ListUtils.isEmpty(compositions) ? null : compositions.find(mr => mr.colorCode === sku.colorCode);
    // フォーム設定値取得
    const registeredValue = this.generateFormSetValue(compositionRecord);

    return {
      id: registeredValue.id,
      check: registeredValue.check,
      colorCode: sku.colorCode,
      colorName: sku.colorName,
      approvalUserAccountName: registeredValue.approvalUserAccountName,
      approvalUserName: registeredValue.approvalUserName,
      approvalAt: registeredValue.approvalAt,
      memo: registeredValue.memo,
      updatedAt: registeredValue.updatedAt
    } as MisleadingRepresentationInspectionForm;
  }

  /**
   * 組成検査FormArrayにFormGroupを追加する.
   * @param value 設定値
   * @returns 追加したFormGroup
   */
  private addCompositionInspection = (value: MisleadingRepresentationInspectionForm): void =>
    this.compositionInspectionsArray.push(this.generateCompositionInspectionFormGroup(value))

  /**
   * 組成FormGroupを作成する.
   * @param value 設定値
   * @returns 組成FormGroup
   */
  private generateCompositionInspectionFormGroup = (value: MisleadingRepresentationInspectionForm): FormGroup =>
    this.fb.group({
      id: value.id, // 優良誤認承認情報ID
      misleadingRepresentationType: MisleadingRepresentationType.COMPOSITION, // 優良誤認検査対象区分
      colorCode: value.colorCode,  // カラーコード
      colorName: value.colorName,  // カラー名称
      check: value.check,  // チェック
      approvalUserAccountName: value.approvalUserAccountName, // 承認者アカウント名
      approvalUserName: value.approvalUserName, // 承認者アカウント名称
      approvalAt: value.approvalAt,  // 承認日
      memo: value.memo,  // メモ
      updatedAt: value.updatedAt,  // 更新日時
    })

  /**
   * 指定した優良誤認検査対象区分に該当するレコードを抽出する.
   * @param data 優良誤認検査データ
   * @param misleadingRepresentationType 優良誤認検査対象区分
   * @returns MisleadingRepresentation
   */
  private findTargetMisleadingRepresentationType = (data: ItemMisleadingRepresentation) =>
    (misleadingRepresentationType: MisleadingRepresentationType): MisleadingRepresentation =>
      ListUtils.isEmpty(data.misleadingRepresentations) ? null :
        data.misleadingRepresentations.find(mr => misleadingRepresentationType === mr.misleadingRepresentationType)

  /**
   * 組成検査対象区分に該当するレコードを抽出する.
   * @param data 優良誤認検査データ
   * @returns MisleadingRepresentation[]
   */
  private filterItemMisleadingRepresentationByComposition = (data: ItemMisleadingRepresentation)
    : MisleadingRepresentation[] =>
      ListUtils.isEmpty(data.misleadingRepresentations) ? null :
        data.misleadingRepresentations.filter(mr => MisleadingRepresentationType.COMPOSITION === mr.misleadingRepresentationType)

  /**
   * チェックボックス押下時の処理.
   * @param check チェック状態
   * @param fCtrl フォームコントロール
   */
  onCheck = (check: boolean, fCtrl: AbstractControl): void => {
    if (check) {
      fCtrl.patchValue({
        approvalUserAccountName: this._approvalUserAccountName,
        approvalUserName: this._approvalUserName,
        approvalAt: DateUtils.convertDate(new Date())
      });
      return;
    }

    fCtrl.patchValue({
      approvalUserAccountName: null,
      approvalUserName: null,
      approvalAt: null
    });
  }
}
