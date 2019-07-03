const required = ['full_name'];

export default function createEmployee(values) {
  const errors = {};

  required.forEach((fieldName) => {
    if (!values[fieldName]) {
      errors[fieldName] = 'Required';
    }
  });

  if (values.teams) {
    const hasATeam = Object.keys(values.teams).reduce(
      (prev, curr) => prev || !!values.teams[curr],
      false,
    );

    if (!hasATeam) {
      errors.teams = 'Select at least one team';
    }
  }

  return errors;
}
