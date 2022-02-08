DECLARE INPUT $X BYTES

DECLARE $Y INTEGER
DECLARE $Z INTEGER
DECLARE $F BOOLEAN

BEGIN.
  PRINT * Main program starts initializing
  PERFORM INIT
  PRINT * Main program computes
  PERFORM DEMO
  PERFORM DUMMY
  PERFORM DUMMY IF $F
  PERFORM PRINT WHILE $F
  PRINT * End of Main program

INIT.
  STORE IN $F 1

PRINT.
  PRINT Printing $X ...
  COMPT IN $Y $Y prec
  STORE IN $F $Y

DEMO.
  CALL subprog $X >> $X1 $Y << $A
  COMPT IN $Y $Y succ

DUMMY.
  PRINT (hey don't mind me, I'm just a dummy print)

