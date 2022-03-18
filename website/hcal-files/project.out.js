var bytelength = (function() {
var def =
function({ $CHUNK }) { this.printer = new Print();
  this.$LENGTH = Calang['IntegerValue'].newInstance();
  this.$CHUNK = Calang['BytesValue'].newInstance();
    this.$CHUNK.setValue($CHUNK);
};
def.prototype = {
  __START:  function() {
this.$LENGTH.setValue(this.$CHUNK.sendMessage("|.|", []));
  },
  run: async function() { this.__START(); this.printer.flush(); return { $LENGTH:this.$LENGTH }; }
};
return def; })();
var password_input = (function() {
var def =
function({  }) { this.printer = new Print();
  this.$MODAL_ELEMENT = Calang['ModalElementValue'].newInstance();
  this.$TEXT_RECORD = Calang['BytesValue'].newInstance();
  this.$MODAL_RESULT = Calang['BooleanValue'].newInstance();
  this.$CLICK_PROGRAM = Calang['ProgramValue'].newInstance();
  this.$TEXT_RECORD = Calang['BytesValue'].newInstance();
};
def.prototype = {
  __MODAL_CLOSE:  function() {
this.$MODAL_ELEMENT.setValue(this.$MODAL_ELEMENT.sendMessage("close!", []));
  },
  __MODAL_OPEN:  function() {
this.$MODAL_ELEMENT.setValue(this.$MODAL_ELEMENT.sendMessage("display!", []));
  },
  __START: async function() {
this.__MODAL_OPEN()
this.$CLICK_PROGRAM.setValue(this.$MODAL_ELEMENT.sendMessage("...", []));
await this.$CLICK_PROGRAM.getValue().bindWith({

}).run().then(__ => {
this.$MODAL_RESULT.setValue(__.$RES);
});

if(this.$MODAL_RESULT.getValue()) this.__USER_CONFIRMS(); else this.__USER_CANCELS();
this.__MODAL_CLOSE()
  },
  __USER_CANCELS:  function() {
this.printer.append(`Good bye user, you'll be missed`);
  },
  __USER_CONFIRMS:  function() {
this.$TEXT_RECORD.setValue(this.$MODAL_ELEMENT.sendMessage("?", []));
this.printer.append(`Warm greeting, password is ${this.$TEXT_RECORD.getValue()}`);
  },
  run: async function() { await this.__START(); this.printer.flush(); return { $TEXT_RECORD:this.$TEXT_RECORD }; }
};
return def; })();
var prog = (function() {
var def =
function({  }) { this.printer = new Print();
  this.$MESSAGE = Calang['BytesValue'].newInstance();
  this.$LENGTH = Calang['IntegerValue'].newInstance();
};
def.prototype = {
  __BEGIN: async function() {
await new password_input({

}).run().then(__ => {
this.$MESSAGE.setValue(__.$TEXT_RECORD);
});

await new bytelength({
$CHUNK:this.$MESSAGE
}).run().then(__ => {
this.$LENGTH.setValue(__.$LENGTH);
});

await new tower({
$HEIGHT:this.$LENGTH
}).run().then(__ => {

});

  },
  run: async function() { await this.__BEGIN(); this.printer.flush(); return {  }; }
};
return def; })();
var tower = (function() {
var def =
function({ $HEIGHT }) { this.printer = new Print();
  this.$HEIGHT = Calang['IntegerValue'].newInstance();
    this.$HEIGHT.setValue($HEIGHT);
  this.$LOCAL_HEIGHT = Calang['IntegerValue'].newInstance();
  this.$CURSOR = Calang['IntegerValue'].newInstance();
  this.$FLAG = Calang['BooleanValue'].newInstance();
};
def.prototype = {
  __PRINT_LINE:  function() {
this.$CURSOR.setValue("1");
this.$FLAG.setValue("1");
while(this.$FLAG.getValue()) this.__PRINT_COLUMN();
this.printer.append(`\n`);
this.$FLAG.setValue(this.$HEIGHT.sendMessage("NEQ", [this.$LOCAL_HEIGHT]));
this.$LOCAL_HEIGHT.setValue(this.$LOCAL_HEIGHT.sendMessage("SUCC", []));
  },
  __MAIN:  function() {
this.$LOCAL_HEIGHT.setValue("1");
this.$FLAG.setValue(this.$HEIGHT);
while(this.$FLAG.getValue()) this.__PRINT_LINE();
  },
  __PRINT_COLUMN:  function() {
this.printer.append(`#`);
this.$FLAG.setValue(this.$LOCAL_HEIGHT.sendMessage("NEQ", [this.$CURSOR]));
this.$CURSOR.setValue(this.$CURSOR.sendMessage("SUCC", []));
  },
  run: async function() { this.__MAIN(); this.printer.flush(); return {  }; }
};
return def; })();