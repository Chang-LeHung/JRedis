import struct
from abc import abstractmethod
from enum import Enum


class JRedisType(Enum):
	STRING = 0

	LIST = 1

	SET = 2

	ZSET = 3

	HASH = 4

	INT = 5

	DOUBLE = 6


class InputByteStream:

	def __init__(self, data: bytearray):
		self.data = data
		self.pos = 0
		self.limit = len(data)

	def read_bytearray(self, length: int) -> bytearray:
		if self.pos + length > self.limit:
			raise IndexError("Out of bounds")
		res = self.data[self.pos: self.pos + length]
		self.pos += length
		return res

	def read_int(self) -> int:
		res = int.from_bytes(self.data[self.pos: self.pos + 4], byteorder="little")
		self.pos += 4
		return res

	def read_double(self) -> float:
		res = struct.unpack("<d", self.data[self.pos: self.pos + 8])[0]
		self.pos += 8
		return res

	def read_long(self) -> int:
		res = int.from_bytes(self.data[self.pos: self.pos + 8], byteorder="little")
		self.pos += 8
		return res

	def read_byte(self) -> int:
		self.pos += 1
		return self.data[self.pos - 1]

	def is_available(self) -> bool:
		return self.pos < self.limit


class OutputByteStream:

	def __init__(self):
		self.data = bytearray()

	def write(self, data: bytearray):
		self.data.extend(data)

	def write_int(self, val: int):
		self.data.extend(val.to_bytes(4, byteorder="little"))

	def write_long(self, val: int):
		self.data.extend(val.to_bytes(8, byteorder="little"))

	def write_double(self, val: float):
		self.data.extend(struct.pack("<d", val))

	def write_byte(self, val: int):
		self.data.extend(val.to_bytes(1, byteorder="little"))

	def get_data(self) -> bytearray:
		return self.data

	def to_input_stream(self):
		return InputByteStream(self.data)


class JRedisObject:

	def __init__(self):
		pass

	@abstractmethod
	def serialize(self, stream: OutputByteStream) -> None:
		pass

	@abstractmethod
	def deserialize(self, stream: InputByteStream) -> None:
		pass


class JRedisString(JRedisObject):
	__slots__ = ("val",)

	def __init__(self, value: str):
		super().__init__()
		self.val = value

	def serialize(self, stream: OutputByteStream) -> None:
		stream.write_byte(JRedisType.STRING.value)
		serial_data = bytearray(self.val.encode("utf-8"))
		stream.write_int(len(serial_data))
		stream.write(serial_data)

	def deserialize(self, stream: InputByteStream) -> None:
		assert stream.read_byte() == JRedisType.STRING.value
		size = stream.read_int()
		self.val = stream.read_bytearray(size).decode("utf-8")

	def __str__(self):
		return self.val

	def __repr__(self):
		return f"\"{self.val}\""


class JRLong(JRedisObject):
	__slots__ = ("val",)

	def __init__(self, val: int):
		super().__init__()
		self.val = val

	def serialize(self, stream: OutputByteStream) -> None:
		stream.write_byte(JRedisType.INT.value)
		stream.write_long(self.val)

	def deserialize(self, stream: InputByteStream) -> None:
		assert stream.read_byte() == JRedisType.INT.value
		self.val = stream.read_long()

	def __str__(self):
		return self.val

	def __repr__(self):
		return self.val


class JRDouble(JRedisObject):
	__slots__ = ("val",)

	def __init__(self, val: float):
		super().__init__()
		self.val = val

	def serialize(self, stream: OutputByteStream) -> None:
		stream.write_byte(JRedisType.DOUBLE.value)
		stream.write_double(self.val)

	def deserialize(self, stream: InputByteStream) -> None:
		assert stream.read_byte() == JRedisType.DOUBLE.value
		self.val = stream.read_double()

	def __repr__(self):
		return self.val

	def __str__(self):
		return self.val


JRedisOK = JRedisString("OK")
JRedisNil = JRedisString("Nil")
JRedisToBeContinued = JRedisString("To be continued ...")

if __name__ == '__main__':
	pass
