var fs=require('fs');
var c  = console;
var dtable = {
    ""   :0b000,
    "M"  :0b001,
    "D"  :0b010,
    "MD" :0b011,
    "A"  :0b100,
    "AM" :0b101,
    "AD" :0b110,
    "AMD":0b111
  }
  
  var jtable = {
    ""   :0b000,
    "JGT":0b001,
    "JEQ":0b010,
    "JGE":0b011,
    "JLT":0b100,
    "JNE":0b101,
    "JLE":0b110,
    "JMP":0b111
  }
  
  var ctable = {
    "0"   :0b0101010,
    "1"   :0b0111111,
    "-1"  :0b0111010,
    "D"   :0b0001100,
    "A"   :0b0110000, 
    "M"   :0b1110000,
    "!D"  :0b0001101,
    "!A"  :0b0110001, 
    "!M"  :0b1110001,
    "-D"  :0b0001111,
    "-A"  :0b0110011,
    "-M"  :0b1110011,
    "D+1" :0b0011111,
    "A+1" :0b0110111,
    "M+1" :0b1110111,
    "D-1" :0b0001110,
    "A-1" :0b0110010,
    "M-1" :0b1110010,
    "D+A" :0b0000010,
    "D+M" :0b1000010,
    "D-A" :0b0010011,
    "D-M" :0b1010011,
    "A-D" :0b0000111,
    "M-D" :0b1000111,
    "D&A" :0b0000000,
    "D&M" :0b1000000,
    "D|A" :0b0010101,
    "D|M" :0b1010101
  }
  
  var symTable = {
    "R0"  :0,
    "R1"  :1,
    "R2"  :2,
    "R3"  :3,
    "R4"  :4,
    "R5"  :5,
    "R6"  :6,
    "R7"  :7,
    "R8"  :8,
    "R9"  :9,
    "R10" :10,
    "R11" :11,
    "R12" :12,
    "R13" :13,
    "R14" :14,
    "R15" :15,
    "SP"  :0,
    "LCL" :1,
    "ARG" :2,
    "THIS":3, 
    "THAT":4,
    "KBD" :24576,
    "SCREEN":16384
  };

var symloaction=0;
var symnum=16;
var word=process.argv[2];
parser();
function parser()
{
    var writeword=word +'.hack';
    fs.createWriteStream(writeword);
    fs.writeFileSync(writeword,'');
    var readword = word + ".asm";
    data=fs.readFileSync(readword);
    data=data.toString();
    var lines=data.split(/\r?\n/);
    for(line of lines)
    {
        line.match(/^([^\/]*)(\/.*)?$/)
        line = RegExp.$1.trim();
        if(line.length!=0)
        {
        pass1(line);
        }
    }
    for(line of lines)
    {
        line.match(/^([^\/]*)(\/.*)?$/)
        line = RegExp.$1.trim();
        if(line.length!=0)
        pass2(line);
    }
}

function zero(line) //補0用的
{
    line= line.toString(2); //轉2進位
    var len=line.length;
    for(let i=0;i<16-len;i++)
    {
        line='0'+ line;
    }
    return line;
}
function pass2(line)
{
	if(line[0]!= '(')
	console.log(line + "  ")
    writeword=word +'.hack';
    if(line[0]=='@') //A指令
    {   
        line=line.substring(1); //line取@後面的字
        if(line.match(/^\d+$/))
        {
            line=line - 0; //line轉數字
            var binary = zero(line); //用zero function 下去補0
            c.log(binary);
        }
        else
        {
            if(symTable[line]==undefined)
            {
                symTable[line]=symnum;
                symnum++;
            }
            var num=symTable[line];
            binary=zero(num);
            c.log(binary);
        }
    } //A指令完成!!
    else  //C指令
    {
        if(line.indexOf('=')!=-1) //判斷line裡面是否有 = 
        {
            var line=line.split('=');
            var binary= 0b111<<13|ctable[line[1]]<<6|dtable[line[0]]<<3|0b000;
            binary=zero(binary);
            console.log(binary);
        }
        else if(line.indexOf(';')!=-1)
        {
            var line=line.split(';');
            var binary= 0b111<<13|ctable[line[0]]<<6|0b000<<3|jtable[line[1]];
            binary=zero(binary);
            console.log(binary);
        }
    }
    if(line[0]!='(')
    fs.appendFileSync(writeword,binary +'\n');
}
function pass1(line)
{
    if(line[0]=='(')
    {
        line.match(/^\(([^\)]+)\)$/);
        var word=RegExp.$1;
        symTable[word]=symloaction;
    }
    else
    symloaction++;   
}
