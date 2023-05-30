/**
 * Optional.
 */
export class Optional<T> {
  constructor(
    public value: T
  ) {
  }

  get(): T {
    return this.value;
  }

  /**
   * 存在する場合は値を返し、それ以外の場合はotherを返します。
   *
   * @param other 存在する値がない場合に返される値、nullも可戻り値:値(存在する場合)、それ以外の場合はother
   * @returns 値(存在する場合)、それ以外の場合はother
   */
  orElse(other: T): T {
    if (this.value == null) {
      return other;
    }
    return this.value;
  }
}
