#!/usr/bin/env python3
import os
import subprocess
import time
import psutil
import sys
import shlex

bin_path = os.getcwd() + '/'
bin_tag = {
    'broker': ['BrokerServer'],
    'log':['LogServer'],
    'db': ['DatabaseServer'],
    'auth': ['AuthServer'],
    'shared':['SharedServer'],
    'velocity':['VelocityServer'],
    'bukkit':['BukkitServer'],
    'all':['BrokerServer', 'LogServer', 'DatabaseServer', 'AuthServer', 'SharedServer', 'VelocityServer', 'BukkitServer']
}

def writePid(fname, pid):
    f = open(fname, 'w')
    f.write(str(pid))
    f.close()

def readPid(fname):
    if not os.path.exists(fname):
        return 0
    f = open(fname, 'r')
    buf = f.read()
    f.close()
    return int(buf)

def Start(svr):
    svr_path = bin_path + svr + '/'
    pid = readPid(svr_path + 'run.pid')
    if pid > 0:
        print("Server [%s,%d] is running." % (svr_path, pid))
        return
    start_file = svr_path + 'start.txt'
    if not os.path.exists(start_file):
        print("start.txt file not found, cannot start.")
        return
    with open(start_file, 'r') as f:
        cmd_line = f.read().strip()
    if not cmd_line:
        print("start.txt is empty, cannot start.")
        return

    fifo_path = svr_path + 'stdin.fifo'
    if not os.path.exists(fifo_path):
        try:
            os.mkfifo(fifo_path)
        except FileExistsError:
            pass

    cmd_args = shlex.split(cmd_line)
    os.chdir(svr_path)
    fifo_fd = os.open(fifo_path, os.O_RDWR)
    p = subprocess.Popen(cmd_args, stdin=fifo_fd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    time.sleep(3)

    print(p.pid)
    writePid(svr_path + 'run.pid', p.pid)

def Stop(svr):
    svr_path = bin_path + svr + '/'
    pid = readPid(svr_path + 'run.pid')
    if pid == 0:
        return
    os.remove(svr_path + 'run.pid')
    fifo_path = svr_path + 'stdin.fifo'
    if os.path.exists(fifo_path):
        try:
            os.remove(fifo_path)
        except Exception:
            pass
    pids = psutil.pids()
    if pid in pids:
        p = psutil.Process(pid)
        p.terminate()
        p.wait(60)

def Restart(svr):
    Stop(svr)
    Start(svr)

def Check(svr):
    svr_path = bin_path + svr + '/'
    pid = readPid(svr_path + 'run.pid')
    pids = psutil.pids()
    if pid > 0:
        if pid not in pids:
            os.remove(svr_path + 'run.pid')
        return
    for id in pids:
        p = psutil.Process(id)
        if p.cwd() + '/' + p.name() == svr_path + "java":
            writePid(svr_path + 'run.pid', id)
            return

def Show(svr):
    svr_path = bin_path + svr + '/'
    pid = readPid(svr_path + 'run.pid')
    if pid == 0:
        return
    pids = psutil.pids()
    if pid not in pids:
        os.remove(svr_path + 'run.pid')
        return
    p = psutil.Process(pid)
    t = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(p.create_time()))
    print("%s=> [%s%s](%d) cpu:%.2f%% memory:%.2f%% theads:%d createtime:%s" % (svr, svr_path, svr, pid, p.cpu_percent(), p.memory_percent(), p.num_threads(), t))

def UStart(svrs):
    for svr in svrs:
        Start(svr)

def UStop(svrs):
    for svr in reversed(svrs):
        Stop(svr)

def URestart(svrs):
    for svr in reversed(svrs):
        Stop(svr)

    for svr in svrs:
        Start(svr)

def UCheck(svrs):
    for svr in svrs:
        Check(svr)

def UShow(svrs):
    for svr in svrs:
        Show(svr)

def manage(op, tag):
    exes = bin_tag[tag]
    if op == 'start':
        UStart(exes)
    elif op == 'stop':
        UStop(exes)
    elif op == 'restart':
        URestart(exes)
    elif op == 'show':
        UShow(exes)
    elif op == 'check':
        UCheck(exes)

def usage():
    print("./manager.py [op] [svr]")
    print("\top: start|stop|restart|show|check")
    print("\tsvr: " + '|'.join(bin_tag.keys()))
    for k, v in bin_tag.items():
        print("\t\t%s: %s" % (k, ','.join(v)))
    print("example: ./manager.py show bukkit")

def main():
    if len(sys.argv) < 3:
        usage()
        return
    if sys.argv[1] not in ['start', 'stop', 'restart', 'show', 'check']:
        usage()
        return
    if sys.argv[2] not in bin_tag.keys():
        usage()
        return
    manage(sys.argv[1], sys.argv[2])

if __name__ == '__main__':
    main()
