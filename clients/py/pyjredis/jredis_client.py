import argparse
import sys

from prompt_toolkit import prompt

from .pyjredis import JRedisClient


def main() -> int:
	parser = argparse.ArgumentParser(description='JRedis client')
	parser.add_argument('--host', type=str, default='127.0.0.1', help='host')
	parser.add_argument('--port', type=int, default=8086, help='port')
	args = parser.parse_args()

	client = JRedisClient(args.host, args.port)
	while True:
		try:
			command = prompt(f'{args.host}:{args.port}> ').strip()
			if len(command) == 0:
				continue
			if command == 'exit':
				break
			ret = client.process_command(command)
			if ret is None:
				break
			print(ret.__repr__())
		except Exception as e:
			if e.__str__() == "Connection closed":
				print("jredis exits normally")
				break
			print(e, file=sys.stderr)
	return 0
