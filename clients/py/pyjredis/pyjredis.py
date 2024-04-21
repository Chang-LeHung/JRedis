import socket
from typing import List, Tuple

from .command import *
from .exceptions import JRedisException
from .objects import (JRedisObject,
					  JRedisType,
					  JRedisString,
					  OutputByteStream,
					  JRedisToBeContinued)


class JRedisClient:
	BUF_SIZE = 1 << 10

	def __init__(self, host, port):
		self.host = host
		self.port = port
		self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		self.sock.connect((self.host, self.port))
		self.cur_data = bytearray()

	@staticmethod
	def parse_command(input_str: str) -> Tuple[int, List[JRedisObject]]:
		lst: List[str] = input_str.split()
		if len(lst) < 1:
			raise JRedisException("Invalid command line")
		command_name = lst[0].upper()
		if command_name not in commands:
			raise JRedisException(f"Invalid command name {lst[0]}")
		cmd = commands[command_name]

		return cmd.value, [JRedisString(s) for s in lst[1:]]

	def launch_request(self, command: int, args: List[JRedisObject]) -> None:
		stream = OutputByteStream()
		stream.write_byte(command)
		for arg in args:
			arg.serialize(stream)
		self.sock.send(stream.get_data())

	def try_parse(self) -> bool:
		idx = 0
		match self.cur_data[idx]:
			case JRedisType.STRING.value:
				val = int.from_bytes(self.cur_data[idx + 1:idx + 5], "little")
				if len(self.cur_data) < val + 5:
					return False
			case JRedisType.SET.value:
				return False
			case JRedisType.ZSET.value:
				return False
			case JRedisType.HASH.value:
				return False
		return True

	def parse(self) -> JRedisObject:
		idx = 0
		match self.cur_data[idx]:
			case JRedisType.STRING.value:
				val = int.from_bytes(self.cur_data[idx + 1:idx + 5], "little")
				if len(self.cur_data) >= val + 5:
					ans = JRedisString(self.cur_data[5: val + 5].decode("utf-8"))
					self.cur_data = self.cur_data[val + 5:]
					return ans
		return JRedisToBeContinued

	def process_command(self, command_line) -> JRedisObject:
		command, args = self.parse_command(command_line)
		self.launch_request(command, args)
		return self.recv_msg()

	def recv_msg(self):
		while True:
			msg = self.sock.recvmsg(self.BUF_SIZE)
			if msg == b"":
				self.close()
				raise JRedisException("Connection closed")
			self.cur_data += msg[0]
			if self.try_parse():
				return self.parse()

	def close(self):
		self.sock.close()


if __name__ == '__main__':
	a = bytearray(b"123")
	b = bytearray(b"123")
	print(a + b)
