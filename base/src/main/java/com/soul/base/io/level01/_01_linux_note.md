
##### computer
- 硬件: 控制器(CPU), 主存储器(内存), 输入输出设备( IO设备: 键盘 鼠标 网卡, 显示器 ), 外部存储器(磁盘)
- kernel: 内核,是一个操作系统的核心. 是基于硬件的第一层软件扩充, 提供操作系统的最基本的功能(最精简的操作系统)
```shell
查看cpu信息(核数:CPU(s))
lscpu
```
**VFS(VirtualFileSystem): 虚拟文件系统**
- 树状结构(tree 命令查看, yum install -y tree), 对系统所有设备的抽象, 在linux中一切皆文件
- FD, 文件描述符, 每一个进程在内核中，都对应有一个“打开文件”数组，存放指向文件对象的指针，而 fd 是这个数组的下标
> fd的分配原则，是从小到大，找到第一个不用的进行分配
 - 每个进程都具有 0、1、2 的fd，分别代表标准输入、标准输出、标准错误输出

```shell
输出重定向(将ls的数据不在屏幕上输出,而是输出到文件中)
ls 1> ls.txt
标准输入来源于ls.txt文件, 标准输出重定向到cat.txt文件
cat 0< ls.txt 1> cat.txt

查看一个不存在的文件
ls notexists.txt 1> ls01.out 2> ls02.out
文件描述符与重定向(> >>)之间不能有空格, 重定向后面接文件,
或加上&接其他文件描述符, 接其他描述符需要在该描述符已经先定义情况下(变量先定义再使用, 2>&1要放在 1> ls03.out 后面)
ls notexists.txt 1> ls03.out 2>&1

将标准输出,错误输出,重定向 至 空设备(/dev/null, 黑洞)
ls >/dev/null 2>&1
ls 1>/dev/null 2>&1

将标准输出,错误输出,重定向 至 ls.out 文件中
ls 1>ls.out 2>&1
less ls.out


```
 - inode, 文件索引节点编号(ls -l 第一列显示),一个系统中具有唯一性( centOS6 inode大小默认256字节 ),
 > 文件的属性存储在inode中, 文件内容存储在block中(磁盘满可能inode 或 block不够用, 看存储的文件都是大文件还都是小文件),
 > 系统读取文件时, 首先通过文件找到inode, 然后才能读取到文件内容block
 - pagecache 页缓存, 默认4k, 从磁盘读取的数据会加载到内存, 以page cache形式存在, 再次读取则速度更快
 - dirty 脏, page cache对应的数据被修改, 则会标记为脏, 此时需要flush到磁盘, 才能持久化修改
> (内核参数控制, 刷入频繁系统性能低, 刷入间隔长易丢数据)




**磁盘挂载**
- 一般 /dev/sda1 磁盘1会挂载内核镜像 ( df -h 查看 )
```shell
/boot 目录形成: 系统启动时, 加载内核, 并将内核文件(/dev/sda1)覆盖到根目录的 /boot 下面
可以查看/boot下面有哪些内容
ls -l /boot
解除 /dev/sda1 到 /boot 的挂载
umount /boot
再查看发现/boot目录空了
ls -l /boot
重新挂载, 并查看文件, 就会发现文件可以重新看到了
mount /dev/sda1 /boot
ls -l /boot
```
**文件类型**
 - 虚拟目录树(对设备解耦, 有一个映射过程, 设备映射成文件) ->  不同设备需要分类 -> 文件类型
```shell
查看目录下详情的第二列中第一个字符, 可区分文件类型:
ls -lia
134293921 dr-xr-x---.  4 root root 4096 Jun 19 16:53 .
128 dr-xr-xr-x. 17 root root 4096 Jun 19 12:44 ..
135331582 -rw-r--r--.  1 root root  176 Dec 29  2013 .bash_profile
135331583 -rw-r--r--.  1 root root  176 Dec 29  2013 .bashrc

.和..为 d 表示是目录(directory)
.bashrc和.bash_profile为 - 表示普通文件(regular file)

通过 file 文件名  (查看文件是什么文件类型)
file ~/.bashrc
显示为: ASCII text
file /var/log/wtmp
显示为: /var/log/wtmp: data

查看(筛选)目录
ls -l /home |grep "^d"
或
tree -Ld 1 /home

符号链接文件 l(link)
硬链接(Hard Link, 同inode号, 多个打开路径, 原文件被删, 硬链接仍可使用) 
软连接(符号链接  Soft Link or Symbolic Link, 快捷方式原文件被删则链接失效)
find /etc/ -type l -name "init.d"|xargs ls -ld
查看源文件和连接文件(ls可以接多个文件,空格分隔)     
ls -l /etc/rc.d/init.d /etc/init.d -d
创建链接文件
touch 1.txt
ln 1.txt 2.txt
ln -s 1.txt 3.txt
注意: 目录只能创建软链接,不能创建硬链接
(针对用户,系统有自己创建的命令 .  .. 作为硬链接,  硬链接不能跨越文件系统)

b(block) 表示块设备和其他的外围设备,是特殊类型的文件(例:视频, 黄色,磁盘设备)
ls -al /dev/sda
或(磁盘的block设备一般在dev中)
find /dev -type b |xargs ls -al
创建一个块设备
mknod testCreateBlock b 5 1

字符设备 c(character) 一般黄色,主要是光猫之类设备
ls -als /dev/tty
或
find /dev -type c |xargs ls -al

s(socket) 表示Socket文件
创建一个fd=7的socket, 并查看socket连接
ls -l /proc/$$/fd
exec 7<> /dev/tcp/www.baidu.com/80
lsof -op $$

p(named pipe) 表示管道文件
 ```

- 特殊设备
> /dev/zero (无限大的空, 却不怎么占用磁盘空间) 
>  /dev/null (黑洞) 两种特殊的设备 )

**使用块设备创建可执行bash目录测试**
```shell
dd 拷贝数据来生成文件(生成块数据)
 第一个参数 input file, 
 第二个参数 output file,
 第三个参数 block size 是每个块设备的大小, 
 第四个参数 count 是块设备的个数
mkdir /root/testDisk
mkdir /root/testDisk/ooxx
cd testDisk
dd if=/dev/zero of=mydisk.img bs=1048576 count=100

使用 losetup 将 mydisk.img 挂载到 /dev/loop0 (虚拟的设备文件)
losetup /dev/loop0 mydisk.img
格式化
mke2fs /dev/loop0
挂载 ext2 格式的虚拟文件设备 到 /root/testDisk/ooxx
mount -a04ThreadConcurrent ext2 /dev/loop0 /root/testDisk/ooxx
这时候 df -h 查看, 发现以后想访问 root/testDisk/ooxx 系统会帮你找到 /dev/loop0 这块虚拟设备文件
df -h 
whereis bash
可以看到 /usr/bin/bash
cp /usr/bin/bash /root/testDisk/ooxx/bin/
查看bash执行依赖哪些类库
cd bin
查看和bash相关的依赖, 都在/lib64 下, 拷贝到和 bin 同级
ldd bash
cd ..
mkdir lib64
cp /lib64/{libtinfo.so.5,libdl.so.2,libc.so.6,ld-linux-x86-64.so.2} ./lib64

切换根目录, 在切换到的目录启动 bash 程序
chroot /root/testDisk/ooxx
查看当前bash的进程号
echo $$
写文件到根目录
echo "hello my root bash" > /abc.txt
退出程序
echo $$
查看根 / 目录是否有刚刚生成的文件
ll /
查看 /root/testDisk/ooxx 目录, 发现文件在这个目录生成
ll /root/testDisk/ooxx
解除挂载(不能在被挂载的路径里面解除,否则会显示 target is busy )
cd ..
umount /dev/loop0
发现文件都不显示了( /root/testDisk/ooxx 恢复为 原先磁盘对应的目录数据)
ll /root/testDisk/ooxx
```


**文件描述符fd, 偏移量测试**
```shell
lsof查看当前进程打开了哪些文件 ( $$ 是指当前bash的进程 )
lsof -p $$
cd /root/testDisk/ooxx
创建一个9行的文件
cat > ooxx.txt <<EOF
1
2
3
4
5
6
7
8
9
EOF
通过管道查看文件第8行(管道,将前一个命令的输出重定向到下一个命令的输入)
cd /root/testDisk/ooxx
head -8 ooxx.txt |tail -1
使用8作为文件描述符,去读取 ooxx.txt 中的数据
exec 8< ooxx.txt
查看到当前进程下的fd出现了8这个文件描述符, 且 
ll /proc/$$/fd
查看8文件描述符的 OFFSET 偏移量为 0t0 (0,刚刚打开)
lsof -op $$
查看偏移量变化(从文件描述符8中读取到第一个换行符的数据(read), 将其标准输出的数据赋值给 a )
read a 0<& 8
echo $a
发现偏移量为 0t2 一个1和一个换行符
lsof -op $$

再使用一个文件描述符指向 ooxx.txt
cd /root/testDisk/ooxx
exec 6< ooxx.txt
发现 OFFSET 偏移量依然为 0t0 (0,刚刚打开), 说明每个文件描述符会维护一个对文件读取位置的指针,
这就是为什么可以重复打开一个文件
lsof -op $$

ps: 其他相关操作
备份文件
dd if=/boot/System.map-3.10.0-327.el7.x86_64 of=/boot_map.img
磁盘对拷(拷贝)
dd /dev/sd3 /dev/sd4
创建空镜像文件
dd /dev/zero /test.img
```

**父子进程, 管道测试**
```shell
yum install -y psmisc
查看系统的进程关系
pstree

查看当前shell的进程关系
ps -ef|grep $$

比如当前定义了一个变量
x=100
echo $x
再切换到其他shell进程就无法获取到变量x了
/bin/bash
echo $x
这就是 export 功能(/etc/profile .bash_profile)

注意花括号{  } 需要使用空格和代码块隔开; 代码块,代码在同一个进程中执行
{ echo "gin"; echo "123"; }

下面运行发现 a 最后值还是 1; 因为 | 是解释执行,遇到 | 会启动两个子进程来处理管道两边命令,
处理完后再回到父进程中
a=1
echo $a
{ a=7; echo "gin"; } | cat
echo $a

特殊: $$ 优先级高,会取父进程号, $BASHPID 优先级低,与管道衔接会取子进程id
echo $$ | cat
echo $BASHPID | cat

可以查看到当前shell有 ps 和 grep 两个子进程
ps -ef|grep $$
echo $$  记录父进程进程号
{ echo $BASHPID; read x; } | { cat ; echo $BASHPID; read y; }
新开一个shell连接窗口,查看父进程的进程号
发现执行了 { echo $BASHPID; read x; } | { cat ; echo $BASHPID; read y; } 时,父进程生成了两个子进程

查看子进程的fd文件描述符
ll /proc/子进程id/fd
ll /proc/30490/fd
ll /proc/30491/fd
可以发现
管道前面命令进程的标准输出指向了pipe 282541
1 -> pipe:[282541]
管道后面命令进程的标准输入指向了pipe 282541
0 -> pipe:[282541]
正好解释了管道的实质,通过管道对接两个命令的输出和输入

lsof -op 查看子进程的id
lsof -op 30490
lsof -op 30491
可以看到前面命令
1w  FIFO    0,8    0t0    282541 pipe
输出指向pipe,且是先进先出的队列模式FIFO

可以看到后面命令
0r  FIFO    0,8    0t0    282541 pipe
```

**pagecache 页缓存**
- kernel的折中方案,可以没有缓存,数据直接写入磁盘,但是会频繁读写磁盘, IO延时较高
- 内核参数控制, 刷入频繁系统性能低, 刷入间隔长易丢数据

**app从磁盘读取流程**
- app(软件服务,buffer, 软件的缓存)
- kernel, 内核会从磁盘读取一个pagecache的数据(4k)
- 磁盘(也有缓冲区, 4k对齐)

数据流转: 
app -> int0x80 
-> kernel(pagecache) 
-> CPU(寄存器) 
->磁盘(缓冲区)
->CPU
->kernel(pagecache, 内存) 
 流程很长,实际没必要经过cpu, 所以有了DMA(节约了cpu时间片段)

ps: 
- 协处理器(DMA),通过DMA对来协调,将磁盘数据直接加载至kernel写到内存中
(如果 app 需要读取数据, 先触发80中断, 在pagecache中没有,那么触发缺页异常,这个进程进入挂起状态(进程调度,处理活跃进程)
再由DMA来协助处理磁盘数据加载到pagecache, cpu继续处理其他活跃进程, 待pagecache加载完成,回调修改app对应进程为活跃状态, 可以获取cpu执行权限)
- system call(软中断, int0x80, 十进制为128, int 的系统指令(寄存器);中断描述符(向量表): 0 1 2 ... 128 call back ... 255
- int0x80从用户态(app)切换到内核态