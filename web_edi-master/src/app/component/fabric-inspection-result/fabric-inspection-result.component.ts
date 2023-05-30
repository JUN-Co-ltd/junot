import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormArray } from '@angular/forms';

import { ExceptionUtils } from '../../util/exception-utils';
import { FileUtils } from '../../util/file-utils';

import { ItemSearchConditions } from '../../model/search-conditions';

import { HeaderService } from '../../service/header.service';
import { ItemService } from '../../service/item.service';
import { FabricInspectionResultService } from '../../service/fabric-inspection-result.service';
import { LoadingService } from '../../service/loading.service';
import { FileService } from '../../service/file.service';

import {
  itemRequiredValidator, fileRequiredValidator, itemExistValidator, itemDuplicationValidator
} from './validator/fabric-inspection-validator.directive';

@Component({
  selector: 'app-fabric-inspection-result',
  templateUrl: './fabric-inspection-result.component.html',
  styleUrls: ['./fabric-inspection-result.component.scss']
})
export class FabricInspectionResultComponent implements OnInit {

  private readonly PART_NO_MAX_COUNT = 5; // 画面に表示する１枠の品番の最大数

  isAfterRegist = false;  // 登録直後か

  overall_susses_msg_code = ''; // 正常系のメッセージコード
  overall_error_msg_code = '';  // エラーメッセージコード

  mainForm: FormGroup;  // メインのフォーム
  submitted = false;    // submitボタン押下したか
  isBtnLock = false;    // 登録処理中にボタンをロックするためのフラグ

  private activeFileCount = 0;                // 添付中ファイル数
  private readonly MAX_FILES = 5;             // 添付できるファイル数の上限
  private readonly MAX_FILE_SIZE = 10000000;  // 添付できるファイルサイズの上限

  constructor(
    private fileService: FileService,
    private formBuilder: FormBuilder,
    private headerService: HeaderService,
    private itemService: ItemService,
    private fabricInspectionResultService: FabricInspectionResultService,
    private loadingService: LoadingService
  ) { }

  ngOnInit() {
    this.headerService.show();
    this.initScreen();
  }

  /**
   * 画面を初期化する。
   */
  private initScreen(): void {
    this.overall_susses_msg_code = '';
    this.overall_error_msg_code = '';
    this.activeFileCount = 0;
    this.mainForm = this.createForm(); // 画面起動時にフォームを作成する
  }

  /**
   * mainFormの項目の状態を取得する。
   * @return mainForm.controls
   */
  get f(): any { return this.mainForm.controls; }

  /**
   * mainFormのitemFormArray項目の状態を取得する。
   * @return mainForm.get('itemFormArray')['controls']
   */
  get fItemFormArray(): any { return this.mainForm.get('itemFormArray')['controls']; }

  /**
   * mainFormのfileFormArray項目の状態を取得する。
   * @return mainForm.get('fileFormArray')['controls']
   */
  get fFileFormArray(): any { return this.mainForm.get('fileFormArray')['controls']; }

  /**
   * メインのFormGroupを作成する。
   * @returns FormGroup
   */
  private createForm(): FormGroup {

    // 品番とファイルのFormArray作成
    const itemFormArray: FormArray = this.createItemFormArray();  // 品番
    const fileFormArray: FormArray = this.formBuilder.array([]);  // ファイル

    return this.formBuilder.group(
      {
        id: [null],                   // 優良誤認ファイル情報Id
        itemFormArray: itemFormArray, // 品番のFormArray
        fileFormArray: fileFormArray, // ファイルのFormArray
      }, {
        validator: Validators.compose(
          [itemRequiredValidator, fileRequiredValidator, itemExistValidator, itemDuplicationValidator]
        )
      }
    );
  }

  /**
   * 品番のFormArrayを作成して返す。
   * @returns 品番FormArray
   */
  private createItemFormArray(): FormArray {
    const itemFormArray = new FormArray([]);

    // 5つ(画面に表示する１枠の品番の最大数)の品番FormGroupを品番のFormArrayにセット
    for (let i = 0; i < this.PART_NO_MAX_COUNT; i++) {

      // 品番のFormGroup作成
      const itemForm = this.formBuilder.group({
        id: [null],           // 品番ID
        partNo: [null],       // 品番
        productName: [null],  // 品名
        errMSG: [null],       // 品名取得失敗時のエラーメッセージ
      });

      itemFormArray.push(itemForm);
    }
    return itemFormArray;
  }

  /**
   * ファイルドロップのエリアでクリックした際のイベント。
   * input type='file'タグのclickイベントを起動する。
   */
  onKickFileEvent(): void {
    if (this.isAfterRegist) { return; } // 登録後は添付不可
    document.getElementById('file').click();
  }

  /**
   * ファイルの入力項目(input type='file'タグ)クリック時のイベント。
   * エクスプローラーからファイルを選択した際、
   * changeイベントを起動する為に前回の入力値をクリアする。
   * (クリアしないと前回と同じファイルが選択できない)
   * @param event ファイルの入力項目のイベント
   */
  onClickFileInput(event): void {
    if (this.isAfterRegist) { return; } // 登録後は添付不可
    event.target.value = '';
  }

  /**
   * 生地検査結果ファイルを選択した時のイベント
   * @param files 添付ファイル
   */
  async onFileSelect(files: any): Promise<any> {
    if (this.isAfterRegist) { return; } // 登録後は添付不可
    console.debug('file:', files);
    ExceptionUtils.displayErrorInfo('fileErrorInfo', '');

    const attachableFileCount = this.MAX_FILES - this.activeFileCount;   // 添付可能ファイル数
    console.debug('添付可能ファイル数:', attachableFileCount);

    if (files.length > attachableFileCount) {
      // 添付可能数を超えている場合、エラーメッセージを出す
      ExceptionUtils.displayErrorInfo('fileErrorInfo', 'ERRORS.ESTIMATES_FILE_COUNT_ERROR');
      return;
    }
    this.activeFileCount = this.activeFileCount + files.length;

    // loading表示。全て処理が終わってからloading非表示にする為同期処理
    this.loadingService.loadStart();
    const uploadPromises = [];
    for (let i = 0; i < files.length; i++) {
      const file = files[i] as File;
      uploadPromises.push(this.fileUploadPromise(file));
    }
    Promise.all(uploadPromises).then(errors => {
      // 全て処理後
      console.debug('all upload done', errors);
      this.setFileUploadErrorMessge(errors);
      console.debug('fFileFormArray:', this.fFileFormArray);
      this.loadingService.loadEnd();
    });
  }

  /**
   * ファイルアップロード時のエラーメッセージを表示する。
   * @param errors エラー情報リスト
   */
  private setFileUploadErrorMessge(errors: any[]): void {
    let errMsg = '';
    errors.forEach(error => {
      if (error != null) {
        errMsg += error + '<br>';
        this.activeFileCount--;
      }
    });
    ExceptionUtils.displayErrorInfo('fileErrorInfo', errMsg);
  }

  /**
   * ファイルアップロード処理
   * 全て処理が完了してから呼び出し元でエラーハンドリングする為、
   * エラーが発生してもrejectではなくresolveでエラーコードを返す。
   * @param file アップロードするファイル
   */
  async fileUploadPromise(file: File): Promise<any> {
    console.debug('fileName:', file.name, ',fileSize:', file.size);
    if (this.MAX_FILE_SIZE <= file.size) {
      // ファイルサイズが10MB以上の場合エラー
      return Promise.resolve('ERRORS.ESTIMATES_FILE_SIZE_ERROR');
    }
    if (!file.name.match(/\.(pdf)$/i)) {
      // 指定の拡張子以外のファイルの場合エラー
      return Promise.resolve('ERRORS.FILE_UNMATCH_EXTENSION');
    }

    const fileFormArray = this.mainForm.controls.fileFormArray as FormArray;
    // ファイルをアップロードする。
    return await this.fileService.fileUpload(file).toPromise().then(x => {
      // アップロードが成功したら、ファイルのFormArrayにセット
      const fileNoId = Number(x['id']);
      const fileForm = this.creatFileFormGroupSettedFile(file, fileNoId);
      fileFormArray.push(fileForm);
      return Promise.resolve(null);
    }, error => {
      let errorMessageCode = 'ERRORS.ANY_ERROR';
      const apiError = ExceptionUtils.apiErrorHandler(error);
      if (apiError != null) {
        errorMessageCode = apiError.viewErrorMessageCode;
      }
      return Promise.resolve(errorMessageCode);
    });
  }

  /**
   * ファイルデータをセットしたファイルのFormGroupを作成
   * @param file ファイル情報
   * @param fileNoId ファイルid
   * @returns ファイルFormGroup
   */
  private creatFileFormGroupSettedFile(file: File, fileNoId: number = null): FormGroup {
    return this.formBuilder.group({
      id: fileNoId,   // ファイルID
      fileData: file, // ファイル
      memo: [null],   // メモ
    });
  }

  /**
   * 添付されたファイルをダウンロードする。
   * @param index fileFormArrayのindex
   */
  onFileDownload(index: number): void {
    const fileFormArrayValues = this.mainForm.controls.fileFormArray.value;
    const fileValue = fileFormArrayValues[index].file as File;
    FileUtils.downloadFile(fileValue, fileValue.name);
  }

  /**
   * 添付ファイル削除アイコン押下時の処理。
   * @param index ファイルのindex
   */
  onFileDelete(index: number): void {
    if (this.isAfterRegist) { return; } // 登録後は削除不可
    this.loadingService.loadStart();

    ExceptionUtils.displayErrorInfo('fileErrorInfo', '');
    const fileFormArray = this.mainForm.controls.fileFormArray as FormArray;
    const file = fileFormArray.value[index];

    this.fileService.deleteFile(file.id).subscribe(() => {
      console.debug('delete success:');
      this.activeFileCount--;
      fileFormArray.removeAt(index);
      this.loadingService.loadEnd();
    }, error => {
      // 失敗
      console.debug('delete fail:');
      this.handelApiError(error);
      this.loadingService.loadEnd();
    });
  }

  /**
   * 品番変更時処理
   *
   * 入力された品番をキーに、引数の品番の検索を行う。
   * 品番が存在する場合は、品番配列の引数のindexの位置にある
   * FormGroupに品名をセットする。
   * 品番が存在しない等のエラーの場合は、品番配列の引数のindexの位置にある
   * FormGroupにエラーメッセージコードをセットする。
   *
   * @param partNo 品番
   * @param index 品番配列のindex
   */
  onChangePartNo(partNo: string, index: number): void {
    this.overall_error_msg_code = '';               // 画面全体のエラーメッセージクリア

    // 検索のため、全半角ハイフン、長音、全角ダッシュ、全角マイナス、全半角スペースがあれば削除する。
    partNo = partNo.replace(/[-‐—−ー―－　 ]/g, '');
    const partNoList = this.mainForm.controls.itemFormArray as FormArray;
    const currentItem = partNoList.at(index);

    // 品番情報初期化
    currentItem.patchValue({ id: null });
    currentItem.patchValue({ productName: null });
    currentItem.patchValue({ errMSG: null });       // 品番個別のエラーメッセージクリア

    // 品番が8桁未満だったら検索せずに終了
    if (partNo.length < 8) { return; }

    // 品番情報検索処理
    this.itemService.getItemSearch({ partNo: partNo } as ItemSearchConditions).toPromise().then(
      item => {
        const itemList = item['items'];
        console.debug('品番取得:', itemList);
        if (itemList.length <= 0) {
          // 検索結果なし
          // 該当品番のFormGoupにエラーメッセージをセット
          currentItem.patchValue({ errMSG: 'ERRORS.VALIDATE.ITEM_NOT_FOUND' });
          return;
        }
        // 該当品番のFormGoupにidと品名をセット
        currentItem.patchValue({ id: itemList[0].id });                   // 品番ID
        currentItem.patchValue({ productName: itemList[0].productName }); // 品名
      }, error => (this.handelApiError(error))
    );
  }

  /**
   * 生地検査結果ファイル情報登録処理
   */
  onSubmit(): void {
    this.isBtnLock = true;
    this.loadingService.loadStart();
    this.submitted = true;

    this.overall_error_msg_code = '';   // エラーメッセージ初期化
    this.overall_susses_msg_code = '';  // 成功メッセージ初期化
    ExceptionUtils.clearErrorInfo();    // カスタムエラーメッセージクリア

    // バリデーションエラーの時に画面に戻す
    console.debug('バリデーションエラー?:', this.mainForm.invalid);
    if (this.mainForm.invalid) {
      console.debug('this.mainForm:', this.mainForm);
      this.overall_error_msg_code = 'ERRORS.VALID_ERROR';
      this.loadingService.loadEnd();
      this.isBtnLock = false;
      return;
    }

    const formValue = this.mainForm.value;

    this.fabricInspectionResultService.postFabricInspectionResult(formValue).subscribe(misleadingRepresentationFile => {
      console.debug('misleadingRepresentationFile:', misleadingRepresentationFile);
      this.overall_susses_msg_code = 'SUCSESS.MISLEADING_REPRESENTATION_FILE_ENTRY';
      this.loadingService.loadEnd();
      this.isBtnLock = false;
      // 登録後の画面
      this.mainForm.disable();
      this.isAfterRegist = true;
    }, error => {
      this.handelApiError(error);
      this.loadingService.loadEnd();
      this.isBtnLock = false;
    });
  }

  /**
   * APIエラー処理
   * @param error
   */
  private handelApiError(error) {
    console.debug('API Error:', error);
    // フッターにエラーメッセージを表示
    this.overall_error_msg_code = 'ERRORS.ANY_ERROR';
    // API側の業務エラーの場合は画面上にエラーメッセージを表示
    const apiError = ExceptionUtils.apiErrorHandler(error);
    if (apiError != null) {
      ExceptionUtils.displayErrorInfo('apiErrorInfo', apiError.viewErrorMessageCode);
    }
  }

  /**
   * 新規登録ボタン押下時の処理
   */
  onNewEdit(): void {
    this.submitted = false;
    this.isAfterRegist = false;
    this.initScreen();
  }
}
