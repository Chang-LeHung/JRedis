import random
import string
import threading
import time

from pyjredis import jredis_client

benchmark = [(0, 0)] * 50


def generate_random_string(length):
	characters = string.ascii_letters + string.digits
	return ''.join(random.choice(characters) for _ in range(length))


def thread_fn1():
	client = jredis_client.JRedisClient("127.0.01", 8086)
	for _ in range(50000):
		key = generate_random_string(random.randint(5, 50))
		val = generate_random_string(random.randint(10, 100))
		client.process_command(f"set {key} {val}")


def thread_fn2():
	client = jredis_client.JRedisClient("127.0.01", 8086)
	for _ in range(50000):
		key = generate_random_string(random.randint(5, 50))
		client.process_command(f"get {key}")


ths = []
for i in range(50):
	ths.append(threading.Thread(target=thread_fn1))

start = time.time()
for th in ths:
	th.start()

for th in ths:
	th.join()
end = time.time()

print("tps:", 50000 * 50 / (end - start))

start = time.time()
ths = []
for i in range(50):
	ths.append(threading.Thread(target=thread_fn2))

start = time.time()
for th in ths:
	th.start()

for th in ths:
	th.join()
end = time.time()

print("qps:", 50000 * 50 / (end - start))
