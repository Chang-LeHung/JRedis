from .command import *
from .exceptions import *
from .objects import *
from .pyjredis import *


class JRedis:
	def __init__(self, host, port):
		self.client = JRedisClient(host, port)

	def get(self, key) -> JRedisObject:
		cmd = Command.GET.value
		args = [JRedisString(key)]
		self.client.launch_request(cmd, args)
		return self.client.recv_msg()

	def set(self, key: str, val: int|str) -> JRedisObject:
		cmd = Command.SET.value
		self.client.launch_request(cmd, [JRedisString(key), JRedisString(val)])
		return self.client.recv_msg()
