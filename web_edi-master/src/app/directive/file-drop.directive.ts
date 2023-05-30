import { Directive, Output, EventEmitter, HostListener } from '@angular/core';

@Directive({
  selector: '[appFileDrop]'
})
export class FileDropDirective {

  constructor() { }

  /**
   * ファイルドロップ時のイベント
   */
  @Output()
  public fileDropEvent: EventEmitter<File[]> = new EventEmitter<File[]>();

  /**
   * ファイルが要素にドラッグされて重なった時のイベント
   * ドラッグイベントを解除しておかないとドロップイベント時にブラウザがファイルを開く動作をしてしまう
   *
   * @param event イベント
   */
  @HostListener('dragover', ['$event'])
  public onDragOver(event: any): void {
    event.preventDefault();
  }

  /**
   * ファイルドロップ時のイベント
   * 取得したファイルを引数に fileDropEvent イベントを発火させる
   *
   * @param event イベント
   */
  @HostListener('drop', ['$event'])
  public onDrop(event: any): void {
    event.preventDefault();
    this.fileDropEvent.emit(event.dataTransfer.files);
  }

}
