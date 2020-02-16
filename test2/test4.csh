#!/bin/csh

#

set IENC=$1
set OENC=$2

set FILE=$3
set FILE_ENC=${FILE}_${OENC}

iconv -f $IENC -t $OENC $FILE > ${FILE_ENC}

##echo $RET

set FILEPARA=$4

set PARA1=`cat $FILEPARA`
set var1 = 1
set PARA2=""
foreach input ( $PARA1 )
    #echo $input
    ##set PARA2="\$"${var1}",""${PARA2}"
    if ( "${PARA2}" == "" ) then
       set PARA2="${PARA2}"'\$'$var1;
    else
       set PARA2="${PARA2}"'","\$'$var1;
    endif
@ var1 = $var1 + 1
end
echo "${PARA2}"

cat << EOF >! test.awk
BEGIN{
FIELDWIDTHS="$PARA1"
}
{
###print \$1","\$2","\$3","\$4
print $PARA2
}
EOF


set LANG_FLG = OFF
if ( $?LC_ALL ) then
    set _LANG = $LANG
    set LANG_FLG = ON
endif
setenv LC_ALL ASCII

gawk -f test.awk  $FILE_ENC

if ( $LANG_FLG == "ON" ) then
    setenv LC_ALL $_LANG
else
    unsetenv LC_ALL
endif
