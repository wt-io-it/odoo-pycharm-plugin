import odoo.tests


class MyTest(odoo.tests.HttpCase):

    def test_something(self):
        existing_obj = self.env['e<caret>xisting']
        existing_obj.do_something()
