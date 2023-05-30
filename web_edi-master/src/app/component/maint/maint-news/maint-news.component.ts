import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormGroup, FormBuilder, FormControl, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { Observable, of, combineLatest, Subscription } from 'rxjs';
import { tap, flatMap, filter, catchError, finalize } from 'rxjs/operators';
import * as Moment_ from 'moment';

import { NoWhitespaceValidator, DateTimeOverValidator } from './validator/maint-news-validator.directive';
import { SubmitType, Path, PreEventParam, ValidatorsPattern } from 'src/app/const/const';
import { MaintNews } from 'src/app/model/maint/maint-news';
import { HeaderService } from 'src/app/service/header.service';
import { SessionService } from 'src/app/service/session.service';
import { LoadingService } from 'src/app/service/loading.service';
import { MessageConfirmModalService } from 'src/app/service/message-confirm-modal.service';
import { MaintNewsService } from 'src/app/service/maint/maint-news.service';
import { NumberUtils } from 'src/app/util/number-utils';
import { FormUtils } from 'src/app/util/form-utils';
import { DateUtils } from 'src/app/util/date-utils';
import { AuthUtils } from 'src/app/util/auth-utils';

const Moment = Moment_;

@Component({
  selector: 'app-maint-news',
  templateUrl: './maint-news.component.html',
  styleUrls: ['./maint-news.component.scss']
})
export class MaintNewsComponent implements OnInit, OnDestroy {
  readonly SUBMIT_TYPE = SubmitType;
  readonly PATH = Path;
  /** 公開開始日が変更された時に、公開開始日に指定された日数を加算して、公開終了日に設定する */
  readonly OPEN_END_ADD_DAY = 14;
  /** 公開開始日が変更された時に、公開開始日に指定された日数を加算して、新着表示終了日に設定する */
  readonly NEW_DISPLAY_END_ADD_DAY = 7;

  /** フッター表示フラグ */
  isShowFooter = false;
  /** 画面を非表示にする */
  invisibled = true;
  /** 画面を非活性にする */
  disabled = true;
  /** フォーム */
  mainForm: FormGroup;
  /** タイトル */
  title: string;
  /** パス（画面の表示モード） */
  path = '';
  /** 画面に表示するメッセージ */
  message = {
    /** フッター */
    footer: {
      /** 正常系 */
      success: { code: '', param: null },
      /** 異常系 */
      error: { code: '', param: null }
    }
  };

  /** ローディングサブスクリプション */
  private loadingSubscription: Subscription;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private formBuilder: FormBuilder,
    private translateService: TranslateService,
    private headerService: HeaderService,
    private sessionService: SessionService,
    private loadingService: LoadingService,
    private messageConfirmModalService: MessageConfirmModalService,
    private maintNewsService: MaintNewsService
  ) { }

  ngOnInit() {
    this.headerService.show();
    const session = this.sessionService.getSaveSession();
    // フッター表示条件: ROLE_EDI
    this.isShowFooter = AuthUtils.isEdi(session);

    // ローディングクリア（親画面のみ）
    this.loadingService.clear();

    // ローディングサブスクリプション開始
    this.loadingSubscription = this.loadingService.isLoading$.subscribe((isLoading) => this.disabled = isLoading);

    // URLとクエリパラメータの変更を検知
    combineLatest([
      this.route.paramMap,
      this.route.queryParamMap
    ]).subscribe(([paramMap, queryParamMap]) => {
      // URLの最後のパス（画面の表示モード）を取得
      this.path = this.route.snapshot.url[this.route.snapshot.url.length - 1].path;
      // URLからidを取得
      const id = NumberUtils.toNumberDefaultIfEmpty(paramMap.get('id'), null);
      // クエリパラメータからpreEvent(遷移前の処理)を取得
      const preEvent = NumberUtils.toNumberDefaultIfEmpty(queryParamMap.get('preEvent'), null);

      let loadingToken = null;
      let isError = false;

      // ローディング開始
      this.loadingService.start().pipe(
        // ローディングトークン待避
        tap((token) => loadingToken = token),
        // メッセージをクリア
        tap(() => this.clearMessage()),
        // 画面非表示
        tap(() => this.invisibled = true),
        // フォームを生成
        tap(() => this.mainForm = this.createFormGroup()),
        // 画面表示に必要な翻訳テキストを取得
        flatMap(() => this.getInitialTranslate()),
        // preEvent(遷移前の処理)によってメッセージを設定
        tap(() => this.setPreEventMessage(preEvent)),
        // idが設定されている場合は、処理を続行
        filter(() => id !== null),
        // お知らせを取得
        flatMap(() => this.getMaintNews(id)),
        // エラーが発生した場合は、エラーモーダルを表示
        catchError((error) => {
          isError = true;
          return this.messageConfirmModalService.openErrorModal(error);
        }),
        finalize(() => {
          // 画面表示（エラーが発生した場合は、非表示のままとする）
          this.invisibled = isError;
          // ローディング停止
          this.loadingService.stop(loadingToken);
        })
      ).subscribe();
    });
  }

  ngOnDestroy() {
    // ローディングサブスクリプション停止
    this.loadingSubscription.unsubscribe();
  }

  /**
   * フォームを取得する。
   * @return mainForm.controls
   */
  get f(): any { return this.mainForm.controls; }

  /**
   * エラー表示の有無を返却する。
   * @param value FormControl
   */
  isErrorDisplay(value: FormControl): boolean {
    return value.invalid && (value.dirty || value.touched);
  }

  /**
   * バリデーションの結果を取得する。バリデーションエラーがある場合、フッターにバリデーションエラーメッセージを表示する。
   * @returns バリデーションの結果
   * - true : 正常
   * - false : エラーあり
   */
  private isValid(): boolean {
    if (this.mainForm.invalid) {
      this.message.footer.error = { code: 'ERRORS.VALID_ERROR', param: null };
      FormUtils.markAsTouchedAllFields(this.mainForm);
      return false;
    }

    return true;
  }

  /**
   * メッセージをクリアする。
   */
  private clearMessage(): void {
    this.message = {
      footer: {
        success: { code: '', param: null },
        error: { code: '', param: null }
      }
    };
  }

  /**
   * 画面表示に必要な翻訳テキストを取得する。
   */
  private getInitialTranslate(): Observable<any> {
    return this.translateService.get('TITLE.NEWS').pipe(
      tap((title) => {
        // タイトルを取得
        this.title = title;
      }
    ));
  }

  /**
   * preEvent(遷移前の処理)によってメッセージを設定する。
   * @param preEvent URLクエリパラメータpreEvent(遷移前の処理)
   */
  private setPreEventMessage(preEvent: number): void {
    switch (preEvent) {
      case PreEventParam.CREATE:
        this.message.footer.success = { code: 'SUCSESS.ENTRY', param: { value: this.title } };
        break;
      case PreEventParam.UPDATE:
        this.message.footer.success = { code: 'SUCSESS.UPDATE', param: { value: this.title } };
        break;
      default:
        this.message.footer.success = { code: '', param: null };
        break;
    }
  }

  /**
   * フォームを作成する。
   */
  private createFormGroup(): FormGroup {
    const openStartAt = new Date();

    return this.formBuilder.group({
      // お知らせ情報ID
      id: [null],
      // タイトル
      title: [null, { validators: [Validators.required, NoWhitespaceValidator] }],
      // 本文
      content: [null, { validators: [Validators.required, NoWhitespaceValidator] }],
      // 公開開始日
      openStartAt: [DateUtils.convertDateToNgbDateStruct(openStartAt), {
        validators: [Validators.required]
      }],
      // 公開開始時間
      openStartTimeAt: ['00:00', {
        validators: [Validators.required, Validators.pattern(ValidatorsPattern.TIME_HH_MM)]
      }],
      // 公開終了日
      openEndAt: [DateUtils.convertDateToNgbDateStruct(this.calcAddDay(openStartAt, this.OPEN_END_ADD_DAY))],
      // 公開終了時間
      openEndTimeAt: ['23:59', {
        validators: [Validators.pattern(ValidatorsPattern.TIME_HH_MM),
        DateTimeOverValidator('openStartAt', 'openStartTimeAt', 'openEndAt')]
      }],
      // 新着表示終了日
      newDisplayEndAt: [DateUtils.convertDateToNgbDateStruct(this.calcAddDay(openStartAt, this.NEW_DISPLAY_END_ADD_DAY)), {
        validators: [Validators.required]
      }],
      // 新着表示終了時間
      newDisplayEndTimeAt: ['23:59', {
        validators: [Validators.required, Validators.pattern(ValidatorsPattern.TIME_HH_MM),
        DateTimeOverValidator('openStartAt', 'openStartTimeAt', 'newDisplayEndAt')],
      }]
    },
      { validator: Validators.compose([]) }
    );
  }

  /**
   * フォームに値を設定する。
   * @param maintNews 値
   */
  private setFormValue(maintNews: MaintNews) {
    this.mainForm.patchValue({
      id: maintNews.id,
      title: maintNews.title,
      content: maintNews.content,
      openStartAt: DateUtils.convertDateToNgbDateStruct(maintNews.openStartAt),
      openStartTimeAt: DateUtils.convertDateToTime(maintNews.openStartAt),
      openEndAt: DateUtils.convertDateToNgbDateStruct(maintNews.openEndAt),
      openEndTimeAt: DateUtils.convertDateToTime(maintNews.openEndAt),
      newDisplayEndAt: DateUtils.convertDateToNgbDateStruct(maintNews.newDisplayEndAt),
      newDisplayEndTimeAt: DateUtils.convertDateToTime(maintNews.newDisplayEndAt)
    });
  }

  /**
   * フォームの値をもとに型変換した値を取得する。
   * @returns 型変換後の値
   */
  private convertFrom(): MaintNews {
    const formControls = this.f;

    return {
      id: formControls.id.value,
      title: formControls.title.value,
      content: formControls.content.value,
      openStartAt: DateUtils.convertNgbDateStructToDate(formControls.openStartAt.value, formControls.openStartTimeAt.value),
      openEndAt: DateUtils.convertNgbDateStructToDate(formControls.openEndAt.value, formControls.openEndTimeAt.value),
      newDisplayEndAt: DateUtils.convertNgbDateStructToDate(formControls.newDisplayEndAt.value, formControls.newDisplayEndTimeAt.value)
    } as MaintNews;
  }

  /**
   * 登録ボタン押下時の処理
   */
  onEntry(): void {
    let loadingToken = null;

    // ローディング開始
    this.loadingService.start().pipe(
      // ローディングトークン待避
      tap((token) => loadingToken = token),
      // メッセージをクリア
      tap(() => this.clearMessage()),
      // バリデーションが正常の場合、処理を続行
      filter(() => this.isValid()),
      // お知らせを作成
      flatMap(() => this.maintNewsService.create(this.convertFrom())),
      // 遷移先のURLを設定
      tap((response) => this.router.navigate(
        ['maint/news', response.id, Path.EDIT],
        { queryParams: { preEvent: PreEventParam.CREATE, t: new Date().valueOf() } })),
      // エラーが発生した場合は、エラーモーダルを表示
      catchError((error) => this.messageConfirmModalService.openErrorModal(error)),
      // ローディング停止
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
  }

  /**
   * 更新ボタン押下時の処理
   */
  onUpdate(): void {
    let loadingToken = null;

    // ローディング開始
    this.loadingService.start().pipe(
      // ローディングトークン待避
      tap((token) => loadingToken = token),
      // メッセージをクリア
      tap(() => this.clearMessage()),
      // バリデーションが正常の場合、処理を続行
      filter(() => this.isValid()),
      // お知らせを更新
      flatMap(() => this.maintNewsService.update(this.convertFrom())),
      // 遷移先のURLを設定
      tap((response) => this.router.navigate(
        ['maint/news', response.id, Path.EDIT],
        { queryParams: { preEvent: PreEventParam.UPDATE, t: new Date().valueOf() } })),
      // エラーが発生した場合は、エラーモーダルを表示
      catchError((error) => this.messageConfirmModalService.openErrorModal(error)),
      // ローディング停止
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
  }

  /**
   * 削除ボタン押下時の処理
   */
  onDelete(): void {
    let loadingToken = null;

    // 確認モーダル表示
    this.messageConfirmModalService.translateAndOpenConfirmModal('INFO.DELETE_COMFIRM_MESSAGE', { value: this.title }).pipe(
      // 確認モーダルの結果が「OK」の場合、処理を続行
      filter((result) => result),
      // ローディング開始＆トークン待避
      flatMap(() => loadingToken = this.loadingService.start()),
      // メッセージをクリア
      tap(() => this.clearMessage()),
      // お知らせを削除
      flatMap(() => this.maintNewsService.delete(this.f.id.value)),
      // 遷移先のURLを設定
      tap(() => this.router.navigate(['maint/news'])),
      // エラーが発生した場合は、エラーモーダルを表示
      catchError((error) => this.messageConfirmModalService.openErrorModal(error)),
      // ローディング停止
      finalize(() => this.loadingService.stop(loadingToken))
    ).subscribe();
  }

  /**
   * お知らせを取得する。
   * @param newsId お知らせのID
   */
  private getMaintNews(newsId: number): Observable<MaintNews> {
    return this.maintNewsService.get(newsId).pipe(
      tap((response) => this.setFormValue(response))
    );
  }

  /**
   * 公開開始日が変更された場合、以下の処理を行う。
   * - 公開終了日 : 公開開始日 + 公開日数 を設定する
   * - 新着表示終了日 : 公開開始日 + 新着表示日数 を設定する
   */
  onOpenStartAtChange(): void {
    const openStartAt = this.f.openStartAt;

    if (openStartAt.valid) {
      this.f.openEndAt.patchValue(
        DateUtils.convertDateToNgbDateStruct(
          this.calcAddDay(DateUtils.convertNgbDateStructToDate(openStartAt.value), this.OPEN_END_ADD_DAY)
        )
      );
      this.f.newDisplayEndAt.patchValue(
        DateUtils.convertDateToNgbDateStruct(
          this.calcAddDay(DateUtils.convertNgbDateStructToDate(openStartAt.value), this.NEW_DISPLAY_END_ADD_DAY)
        )
      );
    }

    // 公開開始日時、公開終了日時、新着表示終了日時のバリデーションを行う
    this.validOpenStartAt();
  }

  /**
   * 公開開始日時、公開終了日時、新着表示終了日時のバリデーションを行う。
   */
  validOpenStartAt(): void {
    const formControls = this.f;

    formControls.openStartAt.updateValueAndValidity();
    formControls.openStartTimeAt.updateValueAndValidity();
    formControls.openEndAt.updateValueAndValidity();
    formControls.openEndTimeAt.updateValueAndValidity();
    formControls.newDisplayEndAt.updateValueAndValidity();
    formControls.newDisplayEndTimeAt.updateValueAndValidity();
  }

  /**
   * フォームの値をDate型に変換する。
   * フォームのバリデーションがエラーの場合は、nullを返却する。
   * @param date 日付
   * @param time 時間
   * @returns Date型に変換した値
   */
  convertFormControlToDate(date: FormControl, time: FormControl): Date {
    if (date.invalid || time.invalid) {
      return null;
    }

    return DateUtils.convertNgbDateStructToDate(date.value, time.value);
  }

  /**
   * 加算した日付を返却する
   * @param date 日付
   * @param addValue 加算する日数
   * @returns 加算した日付
   */
  private calcAddDay(date: Date, addValue: number): Date {
    return new Date(Moment(date).add(addValue, 'd').valueOf());
  }
}
