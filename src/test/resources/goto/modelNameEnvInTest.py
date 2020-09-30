from odoo.tests.common import TransactionCase


class MyTest(TransactionCase):

    def test_something(self):
        existing_obj = self.env['e<caret>xisting']
        existing_obj.do_something()
