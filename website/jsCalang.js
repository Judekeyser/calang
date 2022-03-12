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
  static reduceArgs(base, args, reducer) {
    args = args.map(value => value.getValue());
    args.splice(0, 0, base)
    return args.reduce(reducer);
  }

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
      "NEQ": (v, args) => BooleanValue.of(v != IntegerValue.toInt(args[0])),
      "SUCC": (v, args) => IntegerValue.of(v + 1),
      "PREC": (v, args) => IntegerValue.of(v - 1),
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
    "NEGATE" : (v, args) => BooleanValue.of(!v),
    "XOR"    : (v, args) => BooleanValue.of(TypedValue.reduceArgs(v, args, (a, b) => !!(a ^ b))),
    "AND"    : (v, args) => BooleanValue.of(TypedValue.reduceArgs(v, args, (a, b) => a && b)),
    "OR"     : (v, args) => BooleanValue.of(TypedValue.reduceArgs(v, args, (a, b) => a || b)),
    "XAND"   : (v, args) => BooleanValue.of(TypedValue.reduceArgs(v, args, (a, b) => !(a ^ b))),
    "IMPLIES": (v, args) => BooleanValue.of(!v || args[0].getValue())
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

function Program() {
  this.arguments = {}
}

Program.prototype = {
  run: function() {
    throw "Unable to run undefined program";
  },
  bindWith: function(arguments) {
    this.arguments = arguments;
    return this;
  }
};

class ProgramValue extends TypedValue {
  constructor() {
    super(new Program(), ProgramValue.operatorTable);
  }

  static newInstance() { return new ProgramValue(); }

  static operatorTable = {};
}

/********************************************** */

var Calang = {
  'IntegerValue': IntegerValue,
  'BooleanValue': BooleanValue,
  'BytesValue': BytesValue,
  'ProgramValue': ProgramValue
}