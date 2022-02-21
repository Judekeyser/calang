class Print {
  constructor() {
    this.buffer = [[]];
    this.cursor = 0;
  }

  append(token) {
    if (token == '\n') {
      this.cursor += 1;
      this.buffer.push([]);
    } else {
      var tokens = token.split('\n');
      this.buffer[this.cursor].push(token);
      for(let i = 1; i < tokens.length; i++) {
        this.append('\n');
        this.append(tokens[i]);
      }
    }
  }

  flush() {
    this.buffer.filter(bf => bf[0] != undefined)
               .map(bf => bf.join(" "))
               .forEach(l => console.log(l));
  }
}

class TypedValue {
  constructor(value, operatorTable) {
    this.value = value;
    this.operatorTable = operatorTable;
  }

  getValue() { return this.value; }

  convertFromBytes(data) {
    throw "Unsupported convert from bytes";
  }

  convertFromObject(v) {
    throw `Unsupported convert {v} from object in {this}`;
  }

  sendMessage(operatorName, args) { return this.operatorTable[operatorName](this.getValue(), args); }

  toString() { return new String(this.getValue()); }

  setValue(v) {
    const value = this.value;
    if(value === v || value.constructor == v.constructor)
      this.value = v;
    else if (this.constructor == v.constructor)
      this.value = v.getValue();
    else if (typeof(v) == typeof(''))
      this.value = this.convertFromBytes(v);
    else if (v instanceof TypedValue)
      this.value = this.convertFromObject(v.getValue());
    else
      this.value = this.convertFromObject(v);
  }
}

class IntegerValue extends TypedValue {
  constructor() {
    super(0, IntegerValue.operatorTable);
  }

  static operatorTable = {
      "+": (v, args) => IntegerValue.of(v + args.map(IntegerValue.toInt).reduce((a,b) => a+b)),
      "-": (v, args) => IntegerValue.of(v - args.map(IntegerValue.toInt).reduce((a,b) => a+b)),
      "succ": (v, args) => IntegerValue.of(IntegerValue.toInt(v) + 1),
      "prec": (v, args) => IntegerValue.of(IntegerValue.toInt(v) - 1),
    };

  static toInt(any) {
    var v = new IntegerValue();
    v.setValue(any);
    return v.getValue();
  }

  static newInstance() { return new IntegerValue(); }
  static of(integer) {
    var v = new IntegerValue();
    v.value = integer;
    return v;
  }

  convertFromBytes(data) { return parseInt(data); }
}

class BooleanValue extends TypedValue {
  constructor() {
    super(false, BooleanValue.operatorTable);
  }

  static operatorTable = {
    "NEGATE": (v, args) => BooleanValue.of(!v)
  };

  static newInstance() { return new BooleanValue(); }
  static of(boolean) {
    var v = new BooleanValue();
    v.value = !!boolean;
    return v;
  }

  convertFromBytes(data) {
    if(data.length == 0 || (data.length == 1 && data[0] == '0'))
      return false;
    return true;
  }
  convertFromObject(obj) {
    if(typeof(obj) == typeof(123))
      return obj !== 0
    else throw `Unsupported conversion {obj} -> boolean`;
  }
}

class BytesValue extends TypedValue {
  constructor() {
    super("", BytesValue.operatorTable);
  }

  static operatorTable = {
    "|.|": (v, args) => IntegerValue.of(v.length)
  };

  static newInstance() { return new BytesValue(); }

  convertFromBytes(data) {
    return data;
  }

  convertFromObject(obj) { return ""+obj; }
}

/**************************************************** */



/********************************************** */

var Calang = {
  'IntegerValue': IntegerValue,
  'BooleanValue': BooleanValue,
  'BytesValue': BytesValue
}
















