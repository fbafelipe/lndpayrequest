all:
	docker build -t lndpayrequest .
	docker save lndpayrequest -o lndpayrequest.tar

clean:
	rm -f LndPayRequest.war
	rm -f lndpayrequest.tar
