/**
 * 業務エラークラス
 */
export class BusinessError extends Error {
  __proto__: Error;
  code: string;
  arg: string;
  type: string;

  constructor(code?: string, arg?: string, type?: string) {
    const trueProto = new.target.prototype;
    super();

    this.code = code;
    this.arg = arg;
    this.type = type;

    this.__proto__ = trueProto;
  }
}
